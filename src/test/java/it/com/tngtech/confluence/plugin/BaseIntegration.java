package it.com.tngtech.confluence.plugin;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;
import com.atlassian.confluence.plugin.functest.helper.PageHelper;
import com.atlassian.confluence.plugin.functest.helper.SpaceHelper;

public abstract class BaseIntegration extends AbstractConfluencePluginWebTestCase {
    private long idOfPageContainingMacro;
    private static final String MACROSTRING = "multivote";
    protected static final String TALK_ID = "1000";
    protected static final String LINK_ID = "techday." + TALK_ID;
    protected static final String XPATH_LINE_CLASS = "//div[@class='wiki-content']/table/tbody/tr";
    protected static final String AUDIENCE_XPATH = "//td[@id='audience." + TALK_ID + "']";
    private static final String header = "|| id || name || author || description ||\n";
    private static final String CONTENT = "{" + MACROSTRING + ":tableID}" + "\n" + header +
            "|" + TALK_ID + " | TalkName | TalkAutor | TalkAnmerkung |\n{" + MACROSTRING + "}";

    protected void setUp() throws Exception {
        final SpaceHelper spaceHelper;
        final PageHelper pageHelper;

        super.setUp();

        spaceHelper = getSpaceHelper();
        spaceHelper.setKey("TST");
        spaceHelper.delete(); // if it fails, we're fine

        spaceHelper.setName("Test Space");
        spaceHelper.setDescription("Test Space For TechDay Macro");

        assertTrue(spaceHelper.create());

        pageHelper = getPageHelper();
        pageHelper.setSpaceKey(spaceHelper.getKey());
        pageHelper.setTitle("Techday Macro Test");
        pageHelper.setContent(StringUtils.EMPTY);

        assertTrue(pageHelper.create());

        idOfPageContainingMacro = pageHelper.getId();
    }

    protected void tearDown() throws Exception {
        assertTrue(getSpaceHelper("TST").delete());
        super.tearDown();
    }

    public PageHelper createTechDayTable() {
        final PageHelper pageHelper = getPageHelper(idOfPageContainingMacro);

        assertTrue(pageHelper.read());

        pageHelper
                .setContent(CONTENT);

        assertTrue(pageHelper.update());
        gotoPage("/pages/viewpage.action?pageId=" + pageHelper.getId());

        assertTextNotPresent(TALK_ID);
        assertTextPresent("TalkName");
        assertTextPresent("TalkAutor");
        assertTextPresent("TalkAnmerkung");

        return pageHelper;
    }

    protected String getVotedLineClass() {
        return getElementAttributeByXPath(XPATH_LINE_CLASS, "class");
    }

    protected String getAudienceCount() {
        return getElementTextByXPath(AUDIENCE_XPATH);
    }

    protected String getAudience() {
        return getElementAttributeByXPath(AUDIENCE_XPATH, "title");
    }

    // TODO these are several tests in one, but the setup is quite expensive
    // we don't have @BeforeClass, but it can be simulated with TestSuite,
    // see http://stackoverflow.com/questions/3023091/does-junit-3-have-something-analogous-to-beforeclass
    public void testVoting() throws InterruptedException {
        assertNoVote();

        clickVoteLink();

        assertVoted();
        refreshPage();
        assertVoted();

        clickVoteLink();
        Thread.sleep(1000);

        assertNoVote();
        refreshPage();
        assertNoVote();
    }

    protected abstract void assertNoVote();
    protected abstract void clickVoteLink();
    protected abstract void assertVoted();
    protected abstract void refreshPage();
}