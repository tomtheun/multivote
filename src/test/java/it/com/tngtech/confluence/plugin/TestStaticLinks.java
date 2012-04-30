package it.com.tngtech.confluence.plugin;



public class TestStaticLinks extends BaseIntegration {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gotoPage("display/TST/Multivote+Macro+Test");
        ensureClean();
    }

    protected void refreshPage() {
        // not needed, clicking the link refreshes the page anyway
    }

    protected void clickVoteLink(String tableId) {
        clickElementByXPath(voteLink(tableId));
    }

    protected void assertVoted(String tableId) {
        assertEquals("admin", getAudience(tableId));
        assertEquals("1", getAudienceCount(tableId));
        assertEquals(getVotedLineClass(tableId), "interested");
    }

    protected void assertNoVote(String tableId) {
        assertEquals("", getAudience(tableId));
        assertEquals("0", getAudienceCount(tableId));
        assertEquals(getVotedLineClass(tableId), "notInterested");
    }

    protected boolean voted(String tableId) {
        return !getAudienceCount(tableId).equals("0");
    }
}
