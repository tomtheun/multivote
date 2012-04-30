package it.com.tngtech.confluence.plugin;


public class PerformanceTest extends TestJavaScriptLinks {
    protected static final int MAX_USERS = 400;
    
    @Override
    public void testVoting() throws InterruptedException {
        
    }

    /*
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getGroupHelper("group").create();
    }

    public void testPerformance() {
        List<UserHelper> userHelpers = new ArrayList<UserHelper>();

        for (int i=0; i<MAX_USERS; i++) {
            seleniumLogout();
            String username = "user" + i;
            UserHelper userHelper = getUserHelper(username);
            userHelper.delete(); // don't care if it works or not
            userHelper.setName(username);
            userHelper.setFullName("This is user "+i);
            userHelper.setPassword(username);
            userHelper.setEmailAddress("email@example.com");
            assertTrue(userHelper.create());

            userHelpers.add(userHelper);

            selenium.open("pages/viewpage.action?pageId=" + pageHelper.getId());
            seleniumLogin(username, username);

            if (selenium.isElementPresent("a.button-panel-cancel-link")) {
                selenium.click("dont-show-whats-new");
                selenium.click("a.button-panel-cancel-link");
            }
            //selenium.open("pages/viewpage.action?pageId=" + pageHelper.getId());

            clickVoteLink(TABLE_ID1);
            clickVoteLink(TABLE_ID2);
        }
    }

    @Override
    protected void tearDown() {

    }
    */
}
