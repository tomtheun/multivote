package com.tngtech.confluence.plugin.data;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.velocity.htmlsafe.HtmlSafe;
import com.atlassian.user.User;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Talk implements Comparable<Talk> {
    private UserAccessor userAccessor; // TODO this does not belong here
    private String idName;
    private Set<String> audience = new HashSet<String>();
    private List<String> fields;

    @Override
    public int compareTo(Talk other) {
        int audience2 = other.getAudience().size();
        int audience1 = this.getAudience().size();
        if (audience1 < audience2)
            return 1;
        return -1;
    }

    public Talk(String idName, UserAccessor userAccessor) {
        this.idName = idName;
        this.userAccessor = userAccessor;
    }

    public Talk(String idName, List<String> fields, Set<String> audience, UserAccessor userAccessor) {
        this.idName = idName;
        this.fields = fields;
        this.audience = audience;
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

    public String getIdName() {
        return idName;
    }

    public Set<String> getAudience() {
        return audience;
    }

    public void setAudience(Set<String> audience) {
        this.audience = audience;
    }
    
    @HtmlSafe
    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
