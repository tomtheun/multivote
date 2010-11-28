package it.com.tngtech.confluence.techday;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;
import com.atlassian.confluence.plugin.functest.helper.PageHelper;
import com.atlassian.confluence.plugin.functest.helper.SpaceHelper;

public abstract class BaseIntegration extends AbstractConfluencePluginWebTestCase {
    private long idOfPageContainingChartMacro;
    private static final String MACROSTRING = "{techday-plugin}";
    protected static final String TALK_ID = "1000";
    protected static final String LINK_ID = "techday." + TALK_ID;
    protected static final String XPATH_LINE_CLASS = "//div[@class='wiki-content']/table/tbody/tr";
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

    public void testVotingChangesAudience() {
        // TODO Auto-generated method stub
    }

}