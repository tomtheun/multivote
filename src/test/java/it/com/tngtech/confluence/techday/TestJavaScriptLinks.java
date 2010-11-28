package it.com.tngtech.confluence.techday;

import static com.atlassian.selenium.browsers.AutoInstallClient.assertThat;
import static com.atlassian.selenium.browsers.AutoInstallClient.seleniumClient;
//import static com.atlassian.selenium.browsers.AutoInstallClient.seleniumConfiguration;

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
        //seleniumConfiguration().get
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
    
    @Override
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
        assertLineClassEquals("interested");
        clickVoteLink();
        assertLineClassEquals("notInterested");
        clickVoteLink();
        assertLineClassEquals("interested");
    }

    private void assertLineClassEquals(String value) {
        assertThat().attributeContainsValue(XPATH_LINE_CLASS, "class", value);
    }    
    

    private void clickVoteLink() {
        selenium.clickAndWaitForAjaxWithJquery(VOTE_LINK);
    }

    private void assertAudienceEquals(String audience) {
        assertThat().attributeContainsValue(AUDIENCE_LOC, "title", audience);
    }
    
    private void assertAudienceCountEquals(Integer count) {
        assertThat().elementContainsText(AUDIENCE_LOC, count.toString());
    }
}
