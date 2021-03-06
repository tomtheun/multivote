package com.tngtech.confluence.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.cluster.ClusterManager;
import com.atlassian.confluence.cluster.ClusteredLock;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.user.User;
import com.tngtech.confluence.plugin.data.ItemKey;
import com.tngtech.confluence.plugin.data.VoteItem;

public class DefaultMultiVoteService implements MultiVoteService {
    public VoteItem recordInterest(String remoteUser, boolean requestUse, ItemKey key) {
        ClusteredLock lock = getLock(key);
        Set<String> users;
        try {
            lock.lock();
            users = doRecordInterest(remoteUser, requestUse, key);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
        return new VoteItem(key.getItemId(), users);
    }

    private ClusteredLock getLock(ItemKey key) {
        return clusterManager.getClusteredLock("multivote.lock." + key.getTableId() + "." + key.getItemId());
    }

    private Set<String> doRecordInterest(String user, Boolean requestUse, ItemKey key) {
        boolean changed;
        Set<String> users = retrieveAudience(key);
        if (requestUse) {
            changed = users.add(user);
        } else {
            changed = users.remove(user);
        }
        if (changed) {
            persistAudience(key, users);
        }
        return users;
    }

    public Set<String> retrieveAudience(ItemKey key) {
        String usersAsString = contentPropertyManager.getTextProperty(key.getPage(), buildPropertyString(key));
        if (usersAsString == null) {
            usersAsString = "";
        }
        Set<String> users = new TreeSet<String>();
        StringTokenizer userTokenizer = new StringTokenizer(usersAsString, ",");
        while (userTokenizer.hasMoreTokens()) {
            users.add(userTokenizer.nextToken().trim());
        }
        return users;
    }

    private void persistAudience(ItemKey key, Set<String> users) {
        String property = buildPropertyString(key);
        contentPropertyManager.setTextProperty(key.getPage(), property, StringUtils.join(users, ", "));
    }

    private String buildPropertyString(ItemKey key) {
        return "multivote." + key.getTableId() + "." + key.getItemId();
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
        if (user != null) {
            fullName = user.getFullName();
        }
        if (fullName == null) {
            fullName = userName;
        }
        return fullName;
    }
    
    /*
     * injected Services
     */
    private ContentPropertyManager contentPropertyManager;
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
}
