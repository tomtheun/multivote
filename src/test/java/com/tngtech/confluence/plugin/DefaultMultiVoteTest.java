package com.tngtech.confluence.plugin;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.confluence.cluster.ClusterManager;
import com.atlassian.confluence.cluster.ClusteredLock;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.user.UserAccessor;
import com.tngtech.confluence.plugin.data.ItemKey;
import com.tngtech.confluence.plugin.data.VoteItem;

public class DefaultMultiVoteTest {
    private DefaultMultiVote multivote;
    private ClusterManager clusterManager;
    private ContentPropertyManager contentPropertyManager;
    private UserAccessor userAccessor;
    private String user = "user";
    private ContentEntityObject page = mock(ContentEntityObject.class);
    private String tableId = "tableId";
    private String itemId = "itemId";
    private String key = "multivote.tableId.itemId";
    private ClusteredLock lock;

    private ItemKey itemKey;

    @Before
    public void setUp() throws Exception {
        multivote = new DefaultMultiVote();
        clusterManager = mock(ClusterManager.class);
        lock = mock(ClusteredLock.class);
        when(clusterManager.getClusteredLock(anyString())).thenReturn(lock);
        contentPropertyManager = mock(ContentPropertyManager.class);
        userAccessor = mock(UserAccessor.class);

        multivote.setClusterManager(clusterManager);
        multivote.setContentPropertyManager(contentPropertyManager);
        multivote.setUserAccessor(userAccessor);

        itemKey = new ItemKey(page, tableId, itemId);
    }

    @Test
    public void test_voting_locks() {
        multivote.recordInterest(user, true, itemKey);
        verify(lock).lock();
        verify(lock).unlock();
    }

    @Test
    public void test_voting_for_item_persists_audience() {
        multivote.recordInterest(user, true, itemKey);

        verify(contentPropertyManager).setTextProperty(page, key, user);
    }

    @Test
    public void test_voting_when_already_voted_does_not_persist() {
        when(contentPropertyManager.getTextProperty(page, key)).thenReturn("otherUser1, " + user + ", otherUser2");

        multivote.recordInterest(user, false, itemKey);

        verify(contentPropertyManager, never()).setTextProperty(page, "multivote.tableId.itemId", "");
    }

    @Test
    public void test_voting_against_when_voted_before_does_persist() {
        when(contentPropertyManager.getTextProperty(page, key)).thenReturn("otherUser1, " + user + ", otherUser2");

        multivote.recordInterest(user, false, itemKey);

        verify(contentPropertyManager).setTextProperty(page, key, "otherUser1, otherUser2");
    }

    @Test
    public void test_voting_against_item_that_was_not_voted_for_does_not_persist() {
        multivote.recordInterest(user, false, itemKey);

        verify(contentPropertyManager, never()).setTextProperty(page, "multivote.tableId.itemId", "");
    }

    @Test
    public void test_retrieveAudience_empty() {
        Set<String> audience = multivote.retrieveAudience(itemKey);
        verify(contentPropertyManager).getTextProperty(page, key);
        assertThat(audience, hasSize(0));
    }

    @Test
    public void test_retrieveAudience_users() {
        when(contentPropertyManager.getTextProperty(page, key)).thenReturn("user1, user2");
        Set<String> audience = multivote.retrieveAudience(itemKey);
        assertThat(audience, hasSize(2));
        assertThat(audience, hasItems("user1", "user2"));
    }
}
