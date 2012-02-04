package com.tngtech.confluence.plugin.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.confluence.velocity.htmlsafe.HtmlSafe;

public class VoteItem implements Comparable<VoteItem> {
    private String idName;
    private Set<String> audience = new HashSet<String>();
    private List<String> fields;

    @Override
    public int compareTo(VoteItem other) {
        if (this.equals(other)) {
            return 0;
        }
        int audience2 = other.getAudienceCount();
        int audience1 = this.getAudienceCount();
        if (audience1 < audience2) {
            return 1;
        }
        return -1;
    }

    // generated
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((audience == null) ? 0 : audience.hashCode());
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
        result = prime * result + ((idName == null) ? 0 : idName.hashCode());
        return result;
    }

    // generated
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VoteItem other = (VoteItem) obj;
        if (audience == null) {
            if (other.audience != null)
                return false;
        } else if (!audience.equals(other.audience))
            return false;
        if (fields == null) {
            if (other.fields != null)
                return false;
        } else if (!fields.equals(other.fields))
            return false;
        if (idName == null) {
            if (other.idName != null)
                return false;
        } else if (!idName.equals(other.idName))
            return false;
        return true;
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

    public int getAudienceCount() {
        return audience.size();
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
