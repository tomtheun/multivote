package com.tngtech.confluence.techday.data;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.user.User;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Talk {
    @SuppressWarnings("unused")
    private static final Category log = Logger.getLogger(Talk.class);

    private String speaker;
    private String idName;
    private String name;
    private String description;
    private String comment;
    private TalkType type;
    private Set<String> audience = new HashSet<String>();
    private UserAccessor userAccessor;

    public Talk(String idName, String name, String speaker, String description, String comment, TalkType type, UserAccessor userAccessor) {
        super();
        this.idName = idName;
        this.speaker = speaker;
        this.name = name;
        this.comment = comment;
        this.description = description;
        this.type = type;
        this.userAccessor = userAccessor;
    }

    @SuppressWarnings("unused")
    public boolean isInterested(String user) {
        return audience.contains(user);
    }

    @SuppressWarnings("unused")
    public int getTotalAudience() {
        return audience.size();
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

    public String getUsersAsString() {
        List<String> fullNames = new ArrayList<String>();

        for (String userName: audience) {
            fullNames.add(getFullName(userName));
        }
        return StringUtils.join(fullNames, ", ");
    }

    public void addAudience(String user) {
        audience.add(user);
    }

    public void removeAudience(String user) {
        audience.remove(user);
    }

    @SuppressWarnings("unused")
    public String getSpeaker() {
        return speaker;
    }

    public String getIdName() {
        return idName;
    }

    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public String getDescription() {
        return description;
    }

    @SuppressWarnings("unused")
    public String getComment() {
        return comment;
    }

    public TalkType getType() {
        return type;
    }

    public Set<String> getAudience() {
        return audience;
    }
}
