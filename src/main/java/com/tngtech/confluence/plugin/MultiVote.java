package com.tngtech.confluence.plugin;

import static jodd.lagarto.dom.jerry.Jerry.jerry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.jerry.Jerry;
import jodd.lagarto.dom.jerry.JerryFunction;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.cluster.ClusterManager;
import com.atlassian.confluence.cluster.ClusteredLock;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.user.UserAccessor;
import com.tngtech.confluence.plugin.data.VoteItem;

// TODO convert to a proper service, inject..
// TODO check for duplicate IDs in items
public class MultiVote {
    protected ContentPropertyManager contentPropertyManager;
    protected ContentEntityObject contentObject;
    private UserAccessor userAccessor;
    private ClusterManager clusterManager;
    private List<VoteItem> items = new ArrayList<VoteItem>();
    private List<String> header = new ArrayList<String>();
    private String tableId;

    public MultiVote(String tableId, UserAccessor userAccessor, ContentPropertyManager contentPropertyManager,
            ClusterManager clusterManager, ContentEntityObject contentObject) {
        this.tableId = tableId;
        this.contentPropertyManager = contentPropertyManager;
        this.userAccessor = userAccessor;
        this.clusterManager = clusterManager;
        this.contentObject = contentObject;
    }

    public MultiVote(String xhtmlBody, String tableId, UserAccessor userAccessor, ContentPropertyManager contentPropertyManager,
            ContentEntityObject contentObject, ClusterManager clusterManager) {
        this(tableId, userAccessor, contentPropertyManager, clusterManager, contentObject);
        this.items = buildItemsFromBody(xhtmlBody);
    }

    /**
     * This method parses the body of the macro. It assumes that the format is:
     *
     * <pre>
     * |  ID    | header_1 | ( header_n | )+
     * | idName | column_1 | ( column_n | )+
     * </pre>
     *
     * @param body
     *            of the Macro
     * @return list of {@link com.tngtech.confluence.plugin.data.VoteItem}
     */
    private List<VoteItem> buildItemsFromBody(String body) {
        final Jerry xhtml = jerry(body);
        final Jerry lines = xhtml.$("table").find("tr");

        for (Node node: lines.first().children().gt(0).get()) {
            header.add(node.getInnerHtml());
        }

        lines.gt(0).each(new JerryFunction() {
            private String innerHtml(Jerry it, int index) {
                return it.get(index).getInnerHtml().trim();
            }

            @Override
            public boolean onNode(Jerry $this, int index) {
                Jerry children = $this.children();
                List<String> fields = new ArrayList<String>();

                String idName = children.get(0).getTextContent().trim();
                for (int i=1; i<children.length(); i++) {
                    fields.add(innerHtml(children, i));
                }

                VoteItem item = new VoteItem(idName, fields, retrieveAudience(idName), userAccessor);
                items.add(item);
                return true;
            }
        });
        return items;
    }

    void recordInterest(String remoteUser, String requestItem, Boolean requestUse) {
        for (VoteItem item : items) {
            if (item.getIdName().equalsIgnoreCase(requestItem)) {
                ClusteredLock lock = clusterManager.getClusteredLock("multivote.lock." + tableId + "." + item.getIdName());
                try {
                    lock.lock();
			        recordInterest(remoteUser, item, requestUse);
                } finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            }
        }
    }

    private void recordInterest(String user, VoteItem item, Boolean requestUse) {
        boolean changed;
        String id = item.getIdName();
        Set<String> users = retrieveAudience(id);
        if (requestUse) {
            changed = users.add(user);
        } else {
            changed = users.remove(user);
        }
        if (changed) {
            persistAudience(id, users);
            item.setAudience(users);
        }
    }

    Set<String> retrieveAudience(String idName) {
        String usersAsString = contentPropertyManager.getTextProperty(contentObject, buildPropertyString(idName));
        if (usersAsString == null) {
            usersAsString = getUsersAsStringMigration(idName); // TODO remove when migration is done
            // usersAsString = ""; // TODO enable when migration is done
        }
        Set<String> users = new HashSet<String>();
        StringTokenizer userTokenizer = new StringTokenizer(usersAsString, ",");
        while (userTokenizer.hasMoreTokens()) {
            users.add(userTokenizer.nextToken().trim());
        }
        return users;
    }

    private void persistAudience(String id, Set<String> users) {
        String property = buildPropertyString(id);
        contentPropertyManager.setTextProperty(contentObject, property, StringUtils.join(users, ", "));
    }

    private String getUsersAsStringMigration(String idName) {
        String usersAsString = contentPropertyManager.getTextProperty(contentObject, buildMigrationPropertyString(idName));
        if (usersAsString == null) {
            return "";
        } else {
            contentPropertyManager.setTextProperty(contentObject, buildPropertyString(idName), usersAsString);
            return usersAsString;
        }
    }

    static String buildMigrationPropertyString(String idName) {
        return "techday." + idName;
    }

    String buildPropertyString(String idName) {
        return "multivote." + tableId + "." + idName;
    }

    public VoteItem retrieveItem(String itemId) {
        VoteItem item = new VoteItem(itemId, userAccessor);
        item.setAudience(retrieveAudience(itemId));
        items.add(item);
        return item;
    }

    public List<VoteItem> getItems() {
        return items;
    }

    public List<String> getHeader() {
        return header;
    }
}
