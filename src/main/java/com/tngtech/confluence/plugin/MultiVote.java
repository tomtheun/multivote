package com.tngtech.confluence.plugin;

import java.util.List;
import java.util.Set;

import com.atlassian.confluence.core.ContentEntityObject;
import com.tngtech.confluence.plugin.data.VoteItem;

public interface MultiVote {

    /**
     * Vote for a specific Item
     * @param user that votes
     * @param interest
     * @param page the macro is on
     * @param tableId ID of the instance of the multivote macro
     * @param itemId
     */
    void recordInterest(String user, boolean interest, ContentEntityObject page, String tableId, String itemId);

    /**
     * retrieve a vote Item
     *
     * @param page the macro is on
     * @param tableId ID of the instance of the multivote macro
     * @param itemId ID of the Line
     */
    public VoteItem retrieveItem(ContentEntityObject page, String tableId, String itemId);

    /**
     * This method parses the table-header of the macro. It assumes that the format is:
     * <pre>
     * |  ID    | header_1 | ( header_n | )+
     * </pre>
     *
     * @param body of the Macro
     * @return list of {@link com.tngtech.confluence.plugin.data.VoteItem}
     */
    public List<String> buildHeadersFromBody(final String tableId, String body);

    /**
     * This method parses the table-body of the macro. It assumes that the format is:
     * <pre>
     * |  ID    | header_1 | ( header_n | )+
     * | idName | column_1 | ( column_n | )+
     * </pre>
     *
     * @param body of the Macro
     * @return list of {@link com.tngtech.confluence.plugin.data.VoteItem}
     */
    public List<VoteItem> buildItemsFromBody(ContentEntityObject contentObject, final String tableId, String body);
    public String getUserFullNamesAsString(Set<String> audience);
}
