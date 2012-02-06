package com.tngtech.confluence.plugin;

import java.util.Set;

import com.tngtech.confluence.plugin.data.ItemKey;
import com.tngtech.confluence.plugin.data.VoteItem;

public interface MultiVoteService {
    /**
     * Vote for a specific Item
     * @param user that votes
     * @param interest
     * @param key identifying the vote Item
     * @return the Item that was voted for
     */
    VoteItem recordInterest(String user, boolean interest, ItemKey key);

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
