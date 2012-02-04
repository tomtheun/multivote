package it.com.tngtech.confluence.plugin;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;
import com.atlassian.confluence.plugin.functest.helper.PageHelper;
import com.atlassian.confluence.plugin.functest.helper.SpaceHelper;

public abstract class BaseIntegration extends AbstractConfluencePluginWebTestCase {
    private long idOfPageContainingMacro;
    private static final String MACROSTRING = "multivote";
    private static final String HEADER = "|| id || name || author || description ||\n";

    // personalize to talk
    protected static final String ITEM_ID = "1000";

    protected static final String LINK_ID = "multivote.";

    private static final String TABLE_ID1 = "tableID1";
    private static final String TABLE_ID2 = "tableID2";
    private static final String CONTENT1 = buildContent(TABLE_ID1);

    private static final String CONTENT2 = buildContent(TABLE_ID2);


    private static String buildContent(String tableId) {
        return //"{table-plus:sortDescending=true|sortColumn=4}" +
            "{" + MACROSTRING + ":" + tableId + "}" + "\n" + HEADER + "|" + ITEM_ID
	        + " | Column1 | Column2 | Column3 |\n{" + MACROSTRING + "}"
	        //+ "{table-plus}"
	        ;
    }

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
        final SpaceHelper spaceHelper;
        final PageHelper pageHelper;

        super.setUp();

        spaceHelper = getSpaceHelper();
        spaceHelper.setKey("TST");
        spaceHelper.delete(); // if it fails, we're fine

        spaceHelper.setName("Test Space");
        spaceHelper.setDescription("Test Space For Multivote Macro");

        assertTrue(spaceHelper.create());

        pageHelper = getPageHelper();
        pageHelper.setSpaceKey(spaceHelper.getKey());
        pageHelper.setTitle("Multivote Macro Test");
        pageHelper.setContent(StringUtils.EMPTY);

        assertTrue(pageHelper.create());

        idOfPageContainingMacro = pageHelper.getId();
    }

    protected void tearDown() throws Exception {
        assertTrue(getSpaceHelper("TST").delete());
        super.tearDown();
    }

    public PageHelper createMultivoteTable() {
        final PageHelper pageHelper = getPageHelper(idOfPageContainingMacro);

        assertTrue(pageHelper.read());

        pageHelper.setContent(CONTENT1+"\n"+CONTENT2);

        assertTrue(pageHelper.update());
        gotoPage("/pages/viewpage.action?pageId=" + pageHelper.getId());

        assertTextNotPresent(ITEM_ID);
        assertTextPresent("Column1");
        assertTextPresent("Column2");
        assertTextPresent("Column2");

        return pageHelper;
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

    protected abstract void assertNoVote(String id);
    protected abstract void clickVoteLink(String id);
    protected abstract void assertVoted(String id);
    protected abstract void refreshPage();
}
