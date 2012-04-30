package it.com.tngtech.confluence.plugin;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;

public abstract class BaseIntegration extends AbstractConfluencePluginWebTestCase {
    // personalize to talk
    protected static final String ITEM_ID = "1000";

    protected static final String LINK_ID = "multivote.";

    private static final String TABLE_ID1 = "tableID1";
    private static final String TABLE_ID2 = "tableID2";

// Content of page for tests:
//      {multivote:tableID1}
//	    || id || name || author || description ||
//	    | 1000 | Column1 | Column2 | Column3 |
//	    {multivote}
//	    {multivote:tableID1}
//	    || id || name || author || description ||
//	    | 1000 | Column1 | Column2 | Column3 |
//	    {multivote}

    private static String audienceXpath(String tableId) {
        return "//table[@data-tableid='"+ tableId +"']//td[@id='audience." + ITEM_ID + "']";
    }

    protected static String xpathLineClass(String tableId) {
        return "//table[@data-tableid='" + tableId + "']/tbody/tr";
    }

    protected static String voteLink(String tableId) {
        return "//table[@data-tableid='"+ tableId +"']//input[@id='" + LINK_ID + ITEM_ID + "']";
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected String getVotedLineClass(String tableId) {
        return getElementAttributeByXPath(xpathLineClass(tableId), "class");
    }

    protected String getAudienceCount(String id) {
        return getElementTextByXPath(audienceXpath(id));
    }

    protected String getAudience(String tableId) {
        return getElementAttributeByXPath(audienceXpath(tableId), "title");
    }

    // TODO these are several tests in one, but the setup is quite expensive
    // we don't have @BeforeClass, but it can be simulated with TestSuite,
    // see http://stackoverflow.com/questions/3023091/does-junit-3-have-something-analogous-to-beforeclass
    public void testVoting() throws InterruptedException {
        assertNoVote(TABLE_ID1);
        assertNoVote(TABLE_ID2);

        clickVoteLink(TABLE_ID1);

        assertVoted(TABLE_ID1);
        assertNoVote(TABLE_ID2);
        refreshPage();
        assertVoted(TABLE_ID1);
        assertNoVote(TABLE_ID2);

        clickVoteLink(TABLE_ID1);
        Thread.sleep(1000); // TODO

        assertNoVote(TABLE_ID1);
        assertNoVote(TABLE_ID2);
        refreshPage();
        assertNoVote(TABLE_ID1);
        assertNoVote(TABLE_ID2);


        clickVoteLink(TABLE_ID2);
        assertVoted(TABLE_ID2);
        assertNoVote(TABLE_ID1);
        refreshPage();
        assertVoted(TABLE_ID2);
        assertNoVote(TABLE_ID1);

    }

    public void tearDown() throws Exception {
        ensureClean();
    }

    protected void ensureClean() {
        if (voted(TABLE_ID1)) {
	        clickVoteLink(TABLE_ID1);
        }
        if (voted(TABLE_ID2)) {
	        clickVoteLink(TABLE_ID2);
        }
    }

    protected abstract void assertNoVote(String id);
    protected abstract void clickVoteLink(String id);
    protected abstract void assertVoted(String id);
    protected abstract void refreshPage();
    protected abstract boolean voted(String tableId);
}
