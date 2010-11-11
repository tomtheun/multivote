package it.com.tngtech.confluence.techday;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;
import com.atlassian.confluence.plugin.functest.helper.PageHelper;
import com.atlassian.confluence.plugin.functest.helper.SpaceHelper;

/**
 * Testing {@link com.tngtech.confluence.techday.TechdayMacro}
 */
public class TestTechdayMacro extends AbstractConfluencePluginWebTestCase {
    private static final String XPATH_LINE_CLASS = "//div[@class='wiki-content']/table/tbody/tr";
    private long idOfPageContainingChartMacro;
    private final static String MACROSTRING = "{techday-plugin}";
    private final static String TALK_ID = "1000";
    private static final String LINK_ID = "techday." + TALK_ID;
    private static final String AUDIENCE_XPATH = "//td[@id='audience." + TALK_ID + "']";

    protected void setUp() throws Exception {

        final SpaceHelper spaceHelper;
        final PageHelper pageHelper;

        super.setUp();

        spaceHelper = getSpaceHelper();
        spaceHelper.setKey("TST");
        spaceHelper.setName("Test Space");
        spaceHelper.setDescription("Test Space For Chart Macro");

        assertTrue(spaceHelper.create());

        pageHelper = getPageHelper();
        pageHelper.setSpaceKey(spaceHelper.getKey());
        pageHelper.setTitle("Techday Macro Test");
        pageHelper.setContent(StringUtils.EMPTY);

        assertTrue(pageHelper.create());

        idOfPageContainingChartMacro = pageHelper.getId();
    }

    protected void tearDown() throws Exception {
        assertTrue(getSpaceHelper("TST").delete());
        super.tearDown();
    }

    public PageHelper createTechDayTable() {
        final PageHelper pageHelper = getPageHelper(idOfPageContainingChartMacro);

        assertTrue(pageHelper.read());

        pageHelper
                .setContent(MACROSTRING + "\n| " + TALK_ID + " | TalkName | TalkAutor | TALK | | TalkAnmerkung |\n" + MACROSTRING);

        assertTrue(pageHelper.update());
        gotoPage("/pages/viewpage.action?pageId=" + pageHelper.getId());
        
        assertTextPresent("TalkName");
        assertTextPresent("TalkAutor");
        assertTextPresent("TalkAnmerkung");

        return pageHelper;
    }

    public void testVotingAddsToAudience() {
        final PageHelper pageHelper = createTechDayTable();
        gotoPage("/pages/viewpage.action?pageId=" + pageHelper.getId());

        assertEquals("", getAudience());
        clickLink(LINK_ID);
        assertEquals("admin", getAudience());
    }

    public void testVotingChangesAudienceCount() {
        final PageHelper pageHelper = createTechDayTable();
        gotoPage("/pages/viewpage.action?pageId=" + pageHelper.getId());

        assertEquals("0", getAudienceCount());
        clickLink(LINK_ID);
        assertEquals("1", getAudienceCount());
        clickLink(LINK_ID);
        assertEquals("0", getAudienceCount());
    }
    
    public void testVotingChangesLineClass() {
        final PageHelper pageHelper = createTechDayTable();
        gotoPage("/pages/viewpage.action?pageId=" + pageHelper.getId());
        
        assertEquals(getVotedLineClass(), "notInterested");
        clickLink(LINK_ID);
        assertEquals(getVotedLineClass(), "interested");
    }

    private String getVotedLineClass() {
        return getElementAttributByXPath(XPATH_LINE_CLASS, "class");
    }

    private String getAudienceCount() {
        return getElementTextByXPath(AUDIENCE_XPATH);
    }

    private String getAudience() {
        return getElementAttributByXPath(AUDIENCE_XPATH, "title");
    }

    // TODO user full name. "admin" is just the login name
    // change type of line (coloring)

}