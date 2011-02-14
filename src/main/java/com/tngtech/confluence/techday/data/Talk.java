package com.tngtech.confluence.techday.data;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.user.User;
import org.apache.commons.lang.StringUtils;
//import org.apache.log4j.Category;
//import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Talk {
    //private static final Category log = Logger.getLogger(Talk.class);

    private String speaker;
    private String idName;
    private String name;
    private String description;
    private String comment;
    private TalkType type;
    private Set<String> audience = new HashSet<String>();
    private UserAccessor userAccessor; // TODO this does not belong here

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

    public Talk(String idName, UserAccessor userAccessor) {
        this.idName = idName;
        this.userAccessor = userAccessor;
    }

    public boolean isInterested(String user) {
        return audience.contains(user);
    }

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
        return StringUtils.join(audience, ", ");
    }

    public String getUserFullNamesAsString() {
        List<String> fullNames = new ArrayList<String>();

        for (String userName: audience) {
            fullNames.add(getFullName(userName));
        }
        return StringUtils.join(fullNames, ", ");
    }


    public boolean addAudience(String user) {
        return audience.add(user);
    }

    public boolean removeAudience(String user) {
        return audience.remove(user);
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getIdName() {
        return idName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getComment() {
        return comment;
    }

    public TalkType getType() {
        return type;
    }

    public Set<String> getAudience() {
        return audience;
    }
    
    public void setAudience(Set<String> audience) {
        this.audience = audience;
    }
}
