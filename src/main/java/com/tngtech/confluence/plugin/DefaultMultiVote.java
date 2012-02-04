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
import com.atlassian.user.User;
import com.tngtech.confluence.plugin.data.VoteItem;

public class DefaultMultiVote implements MultiVote {
    protected ContentPropertyManager contentPropertyManager;
    private UserAccessor userAccessor;
    private ClusterManager clusterManager;

    public void setContentPropertyManager(ContentPropertyManager contentPropertyManager) {
        this.contentPropertyManager = contentPropertyManager;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public List<String> buildHeadersFromBody(final String tableId, String body) {
        List<String> header = new ArrayList<String>();

        final Jerry xhtml = jerry(body);
        final Jerry lines = xhtml.$("table").find("tr");

        for (Node node: lines.first().children().gt(0).get()) {
            header.add(node.getInnerHtml());
        }

        return header;
    }

    public List<VoteItem> buildItemsFromBody(final ContentEntityObject page, final String tableId, String body) {
	    final List<VoteItem> items = new ArrayList<VoteItem>();
        final Jerry xhtml = jerry(body);
        final Jerry lines = xhtml.$("table").find("tr");

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

                VoteItem item = new VoteItem(idName, fields, retrieveAudience(page, tableId, idName));
                items.add(item);
                return true;
            }
        });
        return items;
    }

    public void recordInterest(String remoteUser, boolean requestUse, ContentEntityObject page, String tableId, String itemId) {
        ClusteredLock lock = clusterManager.getClusteredLock("multivote.lock." + tableId + "." + itemId);
        try {
            lock.lock();
	        doRecordInterest(remoteUser, requestUse, page, tableId, itemId);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    private void doRecordInterest(String user, Boolean requestUse, ContentEntityObject page, String tableId, String id) {
        boolean changed;
        Set<String> users = retrieveAudience(page, tableId, id);
        if (requestUse) {
            changed = users.add(user);
        } else {
            changed = users.remove(user);
        }
        if (changed) {
            persistAudience(page, tableId, id, users);
        }
    }

    private Set<String> retrieveAudience(ContentEntityObject page, String tableId, String idName) {
        String usersAsString = contentPropertyManager.getTextProperty(page, buildPropertyString(tableId, idName));
        if (usersAsString == null) {
            usersAsString = "";
        }
        Set<String> users = new HashSet<String>();
        StringTokenizer userTokenizer = new StringTokenizer(usersAsString, ",");
        while (userTokenizer.hasMoreTokens()) {
            users.add(userTokenizer.nextToken().trim());
        }
        return users;
    }

    private void persistAudience(ContentEntityObject page, String tableId, String id, Set<String> users) {
        String property = buildPropertyString(tableId, id);
        contentPropertyManager.setTextProperty(page, property, StringUtils.join(users, ", "));
    }

    static String buildMigrationPropertyString(String idName) {
        return "techday." + idName;
    }

    String buildPropertyString(String tableId, String idName) {
        return "multivote." + tableId + "." + idName;
    }

    public VoteItem retrieveItem(ContentEntityObject page, String tableId, String itemId) {
        return new VoteItem(itemId, retrieveAudience(page, tableId, itemId));
    }

    public String getUserFullNamesAsString(Set<String> audience) {
        List<String> fullNames = new ArrayList<String>();

        for (String userName: audience) {
            fullNames.add(getFullName(userName));
        }
        return StringUtils.join(fullNames, ", ");
    }

    private String getFullName(String userName) {
        String fullName = userName;
        User user = userAccessor.getUser(userName);
        if (null != user) {
            fullName = user.getFullName();
        }
        if (null == fullName) {
            fullName = userName;
        }
        return fullName;
    }
}
