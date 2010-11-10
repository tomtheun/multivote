package com.tngtech.confluence.techday;

import com.atlassian.confluence.cluster.ClusterManager;
import com.atlassian.confluence.cluster.ClusteredLock;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.user.UserAccessor;
import com.opensymphony.util.TextUtils;
import com.tngtech.confluence.techday.data.Talk;
import com.tngtech.confluence.techday.data.TalkType;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class TechDayService {
    protected ContentPropertyManager contentPropertyManager;
    protected ContentEntityObject contentObject;
    private UserAccessor userAccessor;
    private ClusterManager clusterManager;

    public List<Talk> getTalks() {
        return talks;
    }

    private List<Talk> talks = new ArrayList<Talk>();

    public TechDayService(UserAccessor userAccessor, ContentPropertyManager contentPropertyManager) {
        this.contentPropertyManager = contentPropertyManager;
        this.userAccessor = userAccessor;
    }

    public TechDayService(UserAccessor userAccessor, ContentPropertyManager contentPropertyManager,
            ClusterManager clusterManager, ContentEntityObject contentObject) {
        this(userAccessor, contentPropertyManager);
        this.clusterManager = clusterManager;
        this.contentObject = contentObject;
    }

    public TechDayService(String body, UserAccessor userAccessor, ContentPropertyManager contentPropertyManager,
            ContentEntityObject contentObject, ClusterManager clusterManager) {
        this(userAccessor, contentPropertyManager);
        this.clusterManager = clusterManager;
        this.contentObject = contentObject;
        this.talks = buildTalksFromBody(body);
    }

    /**
     * This method parses the body of the macro. It assumes that the format is:
     * 
     * <pre>
     * idName | name | speaker | type | description | comment
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

        String idName;
        String name;
        String speaker;
        String type;
        String description;
        String comment;

        // Reconstruct the users that have shown interest until now
        for (StringTokenizer stringTokenizer = new StringTokenizer(body, "\r\n"); stringTokenizer.hasMoreTokens();) {
            String line = stringTokenizer.nextToken().trim();
            if (TextUtils.stringSet(line)) {
                StringTokenizer lineTokenizer = new StringTokenizer(line, "|");
                int numberOfTokens = lineTokenizer.countTokens();
                if (numberOfTokens == 6) {
                    idName = lineTokenizer.nextToken().trim();
                    name = lineTokenizer.nextToken().trim();
                    speaker = lineTokenizer.nextToken().trim();
                    type = lineTokenizer.nextToken().trim();
                    description = lineTokenizer.nextToken().trim();
                    comment = lineTokenizer.nextToken().trim();
                    Talk talk = new Talk(idName, name, speaker, description, comment, TalkType.valueOf(type),
                            userAccessor);
                    talk.setAudience(getAudience(idName));
                    talks.add(talk);
                }
            }
        }

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

    void sortTalks() {
        sortTalks(talks);
    }

    void sortTalks(List<Talk> talks) {
        Collections.sort(talks, new Comparator<Talk>() {
            public int compare(Talk o1, Talk o2) {
                int audience2 = o2.getAudience().size();
                int audience1 = o1.getAudience().size();
                if (audience1 < audience2)
                    return 1;
                if (audience1 == audience2)
                    return 0;
                return -1;
            }
        });
        Collections.sort(talks, new Comparator<Talk>() {
            public int compare(Talk o1, Talk o2) {
                return o1.getType().compareTo(o2.getType());
            }
        });
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
        // TreeMap implements SortedMap
        Map<TalkType, List<Talk>> result = new TreeMap<TalkType, List<Talk>>();
        for (Talk talk : talks) {
            if (result.containsKey(talk.getType())) {
                result.get(talk.getType()).add(talk);
            } else {
                List<Talk> list = new ArrayList<Talk>();
                list.add(talk);
                result.put(talk.getType(), list);
            }
        }

        for (Map.Entry<TalkType, List<Talk>> entry : result.entrySet()) {
            sortTalks(entry.getValue());
        }
        return result;
    }
}
