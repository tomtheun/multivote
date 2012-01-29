package it.com.tngtech.confluence.techday;


import com.atlassian.confluence.plugin.functest.helper.PageHelper;

public class TestStaticLinks extends BaseIntegration { 
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final PageHelper pageHelper = createTechDayTable();
        gotoPage("/pages/viewpage.action?pageId=" + pageHelper.getId());
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected void refreshPage() {
        // not needed, clicking the link refreshes the page anyway
    }

    protected void clickVoteLink() {
        clickLink(LINK_ID);
    }

    protected void assertVoted() {
        assertEquals("admin", getAudience());
        assertEquals("1", getAudienceCount());
        assertEquals(getVotedLineClass(), "interested");
    }

    protected void assertNoVote() {
        assertEquals("", getAudience());
        assertEquals("0", getAudienceCount());
        assertEquals(getVotedLineClass(), "notInterested");
    }
}