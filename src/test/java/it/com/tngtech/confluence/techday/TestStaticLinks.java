package it.com.tngtech.confluence.techday;


import com.atlassian.confluence.plugin.functest.helper.PageHelper;

public class TestStaticLinks extends BaseIntegration 
{ 

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
    
    @Override
    public void testVotingChangesAudience() {
        assertEquals("", getAudience());
        clickLink(LINK_ID);
        assertEquals("admin", getAudience());
        clickLink(LINK_ID);
        assertEquals("", getAudience());
    }

    public void testVotingChangesAudienceCount() {
        assertEquals("0", getAudienceCount());
        clickLink(LINK_ID);
        assertEquals("1", getAudienceCount());
        clickLink(LINK_ID);
        assertEquals("0", getAudienceCount());
    }
    
    public void testVotingChangesLineClass() {
        assertEquals(getVotedLineClass(), "notInterested");
        clickLink(LINK_ID);
        assertEquals(getVotedLineClass(), "interested");
        clickLink(LINK_ID);
        assertEquals(getVotedLineClass(), "notInterested");
    }

    // TODO user full name. "admin" is just the login name
    // change type of line (coloring)
}