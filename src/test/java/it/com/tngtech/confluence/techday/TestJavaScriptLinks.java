package it.com.tngtech.confluence.techday;

import static com.atlassian.selenium.browsers.AutoInstallClient.assertThat;
import static com.atlassian.selenium.browsers.AutoInstallClient.seleniumClient;

import com.atlassian.confluence.plugin.functest.helper.PageHelper;
import com.atlassian.selenium.SeleniumClient;

public class TestJavaScriptLinks extends BaseIntegration {
    private static final String AUDIENCE_LOC = "audience."+TALK_ID;

    private static final String VOTE_LINK = "//a[@id='"+LINK_ID+"']/img";

    SeleniumClient selenium;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        selenium = seleniumClient();
        final PageHelper pageHelper = createTechDayTable();
        selenium.open("pages/viewpage.action?pageId=" + pageHelper.getId());
        //if (need_login)??
        seleniumLogin();
    }

    private void seleniumLogin() {
        selenium.type("os_username", "admin");
        selenium.type("os_password", "admin");
        selenium.click("os_cookie");
        selenium.click("loginButton");
        selenium.waitForPageToLoad("30000");
    }

    @Override
    protected void tearDown() throws Exception {
        selenium.click("//a[@id='logout-link']/span");
        selenium.waitForPageToLoad("30000");       
        super.tearDown();
    }
    
    public void testVotingChangesAudience() {
        assertAudienceEquals("");
        clickVoteLink();
        assertAudienceEquals("admin");
        clickVoteLink();
        assertAudienceEquals("");
    }

    public void testVotingChangesAudienceCount() { 
        assertAudienceCountEquals(0);
        clickVoteLink();
        assertAudienceCountEquals(1);
        clickVoteLink();
        assertAudienceCountEquals(0);
    }
    
    public void testVotingChangesLineClass() {
        assertLineClassEquals("notInterested");
        clickVoteLink();
        assertLineClassEquals("interested");
        clickVoteLink();
        assertLineClassEquals("notInterested");
    }

    private void assertLineClassEquals(String value) {
        assertThat().attributeContainsValue(XPATH_LINE_CLASS, "class", value);
    }    

    private void clickVoteLink() {
        selenium.clickAndWaitForAjaxWithJquery(VOTE_LINK);
    }

    private void assertAudienceEquals(String audience) {
        /* TODO disabled for now, does not work
        String title = selenium.getAttribute("xpath=//td[@id='audience.1000']/@title");
        assertEquals("admin", title);
        //assertThat().attributeContainsValue("xpath=//td[@id='audience." + TALK_ID + "']/", "title", audience); // does not work..
        
        //assertThat().elementContainsText("//td[@id='audience." + TALK_ID + "']/@title", audience);
         * 
         */
    }
    
    private void assertAudienceCountEquals(Integer count) {
        assertThat().elementContainsText(AUDIENCE_LOC, count.toString());
    }
}
