package com.tngtech.confluence.techday.data;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class Talk {

    private String speaker;
    private String idName;
    private String name;
    private String description;
    private String comment;
    private TalkType type;
    private Set<String> audience = new HashSet<String>();

    public Talk(String idName, String name, String speaker, String description, String comment, TalkType type) {
        super();
        this.idName = idName;
        this.speaker = speaker;
        this.name = name;
        this.comment = comment;
        this.description = description;
        this.type = type;
    }

    public boolean isInterested(String user) {
        return audience.contains(user);
    }

    public int getTotalAudience() {
        return audience.size();
    }

    public String getUsersAsString() {
        return StringUtils.join(audience, ", ");
    }

    public void addAudience(String user) {
        audience.add(user);
    }

    public void removeAudience(String user) {
        audience.remove(user);
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
}
