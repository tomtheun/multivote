package com.tngtech.confluence.techday;

import static jodd.lagarto.dom.jerry.Jerry.jerry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import jodd.lagarto.dom.jerry.Jerry;
import jodd.lagarto.dom.jerry.JerryFunction;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.cluster.ClusterManager;
import com.atlassian.confluence.cluster.ClusteredLock;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.user.UserAccessor;
import com.tngtech.confluence.techday.data.Talk;
import com.tngtech.confluence.techday.data.TalkType;

// TODO convert to a proper service, inject..
// TODO check for duplicate IDs in talks
public class TechDayService {
    protected ContentPropertyManager contentPropertyManager;
    protected ContentEntityObject contentObject;
    private UserAccessor userAccessor;
    private ClusterManager clusterManager;

    public List<Talk> getTalks() {
        return talks;
    }

    private List<Talk> talks = new ArrayList<Talk>();

    public TechDayService(UserAccessor userAccessor, ContentPropertyManager contentPropertyManager,
            ClusterManager clusterManager, ContentEntityObject contentObject) {
        this.contentPropertyManager = contentPropertyManager;
        this.userAccessor = userAccessor;
        this.clusterManager = clusterManager;
        this.contentObject = contentObject;
    }

    public TechDayService(String xhtmlBody, UserAccessor userAccessor, ContentPropertyManager contentPropertyManager,
            ContentEntityObject contentObject, ClusterManager clusterManager) {
        this(userAccessor, contentPropertyManager, clusterManager, contentObject);
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
     * @return list of {@link com.tngtech.confluence.techday.data.Talk}
     */
    private List<Talk> buildTalksFromBody(String body) {
        final Jerry xhtml = jerry(body);
        final Jerry lines = xhtml.$("table").find("tr");

        lines.each(new JerryFunction() {
            private String innerH(Jerry it, int index) {
                return it.get(index).getInnerHtml().trim();
            }

            @Override
            public boolean onNode(Jerry $this, int index) {
                Jerry children = $this.children();
                String idName = children.get(0).getTextContent().trim();
                String name = innerH(children, 1);
                String speaker = innerH(children, 2);
                String type = children.get(3).getTextContent().trim();
                String description = innerH(children, 4);
                String comment = innerH(children, 5);
                Talk talk = new Talk(idName, name, speaker, description, comment, TalkType.valueOf(type),
                        userAccessor);
                talk.setAudience(getAudience(idName));
                talks.add(talk);
                return true;
            }
        });
        return talks;
    }

    void recordInterest(String remoteUser, String requestTalk, Boolean requestUse) {
        Boolean changed;
        String id;
        for (Talk talk : talks) {
            if (talk.getIdName().equalsIgnoreCase(requestTalk)) {
                id = talk.getIdName();

                ClusteredLock lock = null;
                lock = clusterManager.getClusteredLock("techday.talk.lock." + id);
                try {
                    lock.lock();

                    Set<String> users = getAudience(id);
                    if (requestUse) {
                        changed = users.add(remoteUser);
                    } else {
                        changed = users.remove(remoteUser);
                    }
                    if (changed) {
                        String property = TechDayService.buildPropertyString(talk.getIdName());
                        contentPropertyManager.setTextProperty(contentObject, property, StringUtils.join(users, ", "));
                        talk.setAudience(users);
                    }
                } finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            }
        }
    }

    Set<String> getAudience(String idName) {
        String usersAsString = contentPropertyManager.getTextProperty(contentObject, buildPropertyString(idName));
        Set<String> users = new HashSet<String>();
        if (usersAsString == null) {
            usersAsString = "";
        }
        StringTokenizer userTokenizer = new StringTokenizer(usersAsString, ",");
        while (userTokenizer.hasMoreTokens()) {
            users.add(userTokenizer.nextToken().trim());
        }
        return users;
    }

    static String buildPropertyString(String idName) {
        return "techday." + idName;
    }

    public Talk addTalk(String talkId) {
        Talk talk = new Talk(talkId, userAccessor);
        talk.setAudience(getAudience(talkId));
        talks.add(talk);
        return talk;
    }

    public Map<TalkType, List<Talk>> getTalksByType() {
        Map<TalkType, List<Talk>> result = new TreeMap<TalkType, List<Talk>>();
        for (Talk talk : talks) {
            if (!result.containsKey(talk.getType())) {
                result.put(talk.getType(), new ArrayList<Talk>());
            }
            result.get(talk.getType()).add(talk);
        }

        for (Map.Entry<TalkType, List<Talk>> entry : result.entrySet()) {
            Collections.sort(entry.getValue());
        }
        return result;
    }
}
