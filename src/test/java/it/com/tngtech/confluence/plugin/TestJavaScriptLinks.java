package it.com.tngtech.confluence.plugin;

import static com.atlassian.selenium.browsers.AutoInstallClient.assertThat;
import static com.atlassian.selenium.browsers.AutoInstallClient.seleniumClient;

import com.atlassian.confluence.plugin.functest.helper.PageHelper;
import com.atlassian.selenium.SeleniumClient;

public class TestJavaScriptLinks extends BaseIntegration {
    private static final String AUDIENCE_LOC = "audience."+ITEM_ID;

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

    protected void refreshPage() {
        selenium.refresh();
        selenium.waitForPageToLoad("30000");
    }

    protected void assertVoted() {
        assertAudienceEquals("admin");
        assertAudienceCountEquals(1);
        assertLineClassEquals("interested");
    }

    protected void assertNoVote() {
        assertAudienceEquals("");
        assertAudienceCountEquals(0);
        assertLineClassEquals("notInterested");
    }

    private void assertLineClassEquals(String value) {
        assertThat().attributeContainsValue(XPATH_LINE_CLASS, "class", value);
    }

    protected void clickVoteLink() {
        selenium.clickAndWaitForAjaxWithJquery(VOTE_LINK);
    }

    private void assertAudienceEquals(String audience) {
        if (audience == "") {
            assertFalse(selenium.isElementPresent("//td[@id='audience.1000' and @title!='']"));
            return;
        }

        assertEquals(audience, selenium.getAttribute("//td[@id='audience.1000']/@title"));
    }

    private void assertAudienceCountEquals(Integer count) {
        assertThat().elementContainsText(AUDIENCE_LOC, count.toString());
    }
}
