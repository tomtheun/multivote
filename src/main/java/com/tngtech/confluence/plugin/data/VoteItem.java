package com.tngtech.confluence.plugin.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.velocity.htmlsafe.HtmlSafe;

public class VoteItem implements Comparable<VoteItem> {
    private String idName;
    private Set<String> audience = new HashSet<String>();
    private List<String> fields;

    @Override
    public int compareTo(VoteItem other) {
        int audience2 = other.getAudience().size();
        int audience1 = this.getAudience().size();
        if (audience1 < audience2)
            return 1;
        return -1;
    }

    public VoteItem(String idName, Set<String> audience) {
        this.idName = idName;
        this.audience = audience;
    }

    public VoteItem(String idName, List<String> fields, Set<String> audience) {
        this(idName, audience);
        this.fields = fields;
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

    @HtmlSafe
    public List<String> getFields() {
        return fields;
    }
}
