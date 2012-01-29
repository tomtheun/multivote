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
import com.tngtech.confluence.plugin.data.Talk;

// TODO convert to a proper service, inject..
// TODO check for duplicate IDs in talks
public class MultiVote {
    protected ContentPropertyManager contentPropertyManager;
    protected ContentEntityObject contentObject;
    private UserAccessor userAccessor;
    private ClusterManager clusterManager;
    private List<Talk> talks = new ArrayList<Talk>();
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
        this.talks = buildTalksFromBody(xhtmlBody);
    }

    /**
     * This method parses the body of the macro. It assumes that the format is:
     *
     * <pre>
     * | idName | name | speaker | type | description | comment |
     * </pre>
     *
     * Where type is the text version of the
     * {@link com.tngtech.confluence.techday.data.TalkType} values.
     *
     * @param body
     *            of the Macro
     * @return list of {@link com.tngtech.confluence.plugin.data.Talk}
     */
    private List<Talk> buildTalksFromBody(String body) {
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

                Talk talk = new Talk(idName, fields, retrieveAudience(idName), userAccessor);
                talks.add(talk);
                return true;
            }
        });
        return talks;
    }

    void recordInterest(String remoteUser, String requestTalk, Boolean requestUse) {
        for (Talk talk : talks) {
            if (talk.getIdName().equalsIgnoreCase(requestTalk)) {

                ClusteredLock lock = clusterManager.getClusteredLock("techday.talk.lock." + talk.getIdName());
                try {
                    lock.lock();
			        recordInterest(remoteUser, talk, requestUse);
                } finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            }
        }
    }

    private void recordInterest(String user, Talk talk, Boolean requestUse) {
        boolean changed;
        String id = talk.getIdName();
        Set<String> users = retrieveAudience(id);
        if (requestUse) {
            changed = users.add(user);
        } else {
            changed = users.remove(user);
        }
        if (changed) {
            persistAudience(id, users);
            talk.setAudience(users);
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

    public Talk retrieveTalk(String talkId) {
        Talk talk = new Talk(talkId, userAccessor);
        talk.setAudience(retrieveAudience(talkId));
        talks.add(talk);
        return talk;
    }

    public List<Talk> getTalks() {
        return talks;
    }

    public List<String> getHeader() {
        return header;
    }
}
