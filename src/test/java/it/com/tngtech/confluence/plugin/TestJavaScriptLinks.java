package it.com.tngtech.confluence.plugin;

import static com.atlassian.selenium.browsers.AutoInstallClient.assertThat;
import static com.atlassian.selenium.browsers.AutoInstallClient.seleniumClient;

import com.atlassian.selenium.SeleniumClient;

public class TestJavaScriptLinks extends BaseIntegration {
    private SeleniumClient selenium;
    private static String audienceLoc(String tableId) {
        return "//table[@data-tableid='"+tableId+"']//td[@id='audience."+ITEM_ID+"']";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        selenium = seleniumClient();
        selenium.open("display/TST/Multivote+Macro+Test");

        //if (need_login)??
        seleniumLogin();
        ensureClean();
    }

    private void seleniumLogin() {
        selenium.type("os_username", "admin");
        selenium.type("os_password", "admin");
        selenium.click("os_cookie");
        selenium.click("loginButton");
        selenium.waitForPageToLoad("30000");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        selenium.click("//a[@id='logout-link']/span");
        selenium.waitForPageToLoad("30000");
    }

    protected void refreshPage() {
        selenium.refresh();
        selenium.waitForPageToLoad("30000");
    }

    protected void assertVoted(String tableId) {
        assertAudienceEquals(tableId, "admin");
        assertAudienceCountEquals(tableId, 1);
        assertLineClassEquals(tableId, "interested");
    }

    protected void assertNoVote(String tableId) {
        assertAudienceEquals(tableId, "");
        assertAudienceCountEquals(tableId, 0);
        assertLineClassEquals(tableId, "notInterested");
    }

    private void assertLineClassEquals(String tableId, String value) {
        assertThat().attributeContainsValue(xpathLineClass(tableId), "class", value);
    }

    protected void clickVoteLink(String tableId) {
        selenium.clickAndWaitForAjaxWithJquery(voteLink(tableId));
    }

    private void assertAudienceEquals(String tableId, String audience) {
        if ("".equals(audience)) {
            assertFalse(selenium.isElementPresent("//table[@data-tableid='"+ tableId +"']//td[@id='audience." + ITEM_ID +"' and @title!='']"));
            return;
        }

        assertEquals(audience, selenium.getAttribute("//table[@data-tableid='"+ tableId +"']//td[@id='audience." + ITEM_ID + "']/@title"));
    }

    private void assertAudienceCountEquals(String tableId, Integer count) {
        assertThat().elementContainsText(audienceLoc(tableId), count.toString()); // TODO
    }

    protected boolean voted(String tableId) {
        return !selenium.getText(audienceLoc(tableId)).equals("0");
    }
}
