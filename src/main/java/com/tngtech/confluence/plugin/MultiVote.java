package com.tngtech.confluence.plugin;

import java.util.Set;

import com.tngtech.confluence.plugin.data.ItemKey;
import com.tngtech.confluence.plugin.data.VoteItem;

public interface MultiVote {
    /**
     * Vote for a specific Item
     * @param user that votes
     * @param interest
     * @param key identifying the vote Item
     */
    void recordInterest(String user, boolean interest, ItemKey key);

    /**
     * retrieve a vote Item
     * @param key identifying the vote Item
     */
    VoteItem retrieveItem(ItemKey key);

    /**
     * retrieve the Audience of an item
     * @param key identifying the vote Item
     */
    Set<String> retrieveAudience(ItemKey key);

    /**
     * Get the full names of a set of users
     * @param audience Set of usernames
     */
    String getUserFullNamesAsString(Set<String> audience);
}
