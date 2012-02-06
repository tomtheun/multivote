package com.tngtech.confluence.plugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.confluence.core.ContentEntityObject;
import com.tngtech.confluence.plugin.data.ItemKey;
import com.tngtech.confluence.plugin.data.VoteItem;

public class MultivoteMacroTest {
    private static final String USER_IN_AUDIENCE = "userInAudience";
    private MultivoteMacro macro;
    private String body =
            "<div class='table-wrap'>"
            +"<table class='confluenceTable'><tbody>"
            +"<tr>"
            +"<th class='confluenceTh'> ID </th>"
            +"<th class='confluenceTh'> header1 </th>"
            +"<th class='confluenceTh'> header2 </th>"
            +"</tr>"
            +"<tr>"
            +"<td class='confluenceTd'> 4711 </td>"
            +"<td class='confluenceTd'> bla </td>"
            +"<td class='confluenceTd'> blubb </td>"
            +"</tr>"
            +"<tr>"
            +"<td class='confluenceTd'> 0815 </td>"
            +"<td class='confluenceTd'> bloerk </td>"
            +"<td class='confluenceTd'> plopp </td>"
            +"</tr>"
            +"</tbody></table>"
            +"</div>";
    private ContentEntityObject page;
    private MultiVoteService multiVote;

    @Before
    public void setUp() throws Exception {
        macro = new MultivoteMacro();
        multiVote = mock(MultiVoteService.class);
        Set<String> audience = new TreeSet<String>();
        audience.add(USER_IN_AUDIENCE);
        when(multiVote.retrieveAudience((ItemKey)anyObject())).thenReturn(audience);
        macro.setMultiVote(multiVote);

        page = mock(ContentEntityObject.class);
    }

    @Test
    public void test_header_parsing() {
        List<String> headers = macro.buildHeadersFromBody(body);
        assertThat(headers.get(0), equalTo(" header1 "));
        assertThat(headers.get(1), equalTo(" header2 "));
        assertThat(headers, hasSize(2));
    }

    @Test
    public void test_body_parsing() {
        List<VoteItem> items = macro.buildItemsFromBody(page, "tableId", body);

        VoteItem item = items.get(0);
        assertThat(items, hasSize(2));
        assertThat(item.getIdName(), equalTo("4711"));

        String user = item.getAudience().iterator().next();

        assertThat(user, equalTo(USER_IN_AUDIENCE));
        assertThat(item.getAudienceCount(), equalTo(1));

        List<String> fields = item.getFields();
        assertThat(fields.get(0), equalTo("bla"));
        assertThat(fields.get(1), equalTo("blubb"));
        assertThat(fields, hasSize(2));

        item = items.get(1);
        assertThat(items, hasSize(2));
        assertThat(item.getIdName(), equalTo("0815"));

        user = item.getAudience().iterator().next();

        assertThat(user, equalTo(USER_IN_AUDIENCE));
        assertThat(item.getAudienceCount(), equalTo(1));

        fields = item.getFields();
        assertThat(fields.get(0), equalTo("bloerk"));
        assertThat(fields.get(1), equalTo("plopp"));
        assertThat(fields, hasSize(2));
    }

}
