package it.com.tngtech.confluence.techday;

import java.util.ArrayList;

import net.sourceforge.jwebunit.html.Cell;
import net.sourceforge.jwebunit.html.Row;
import net.sourceforge.jwebunit.html.Table;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;
import com.atlassian.confluence.plugin.functest.helper.PageHelper;
import com.atlassian.confluence.plugin.functest.helper.SpaceHelper;

/**
 * Testing {@link com.tngtech.confluence.techday.TechdayMacro}
 */
public class TestTechdayMacro extends AbstractConfluencePluginWebTestCase {
	private long idOfPageContainingChartMacro;
    private final static String MACROSTRING = "{techday-plugin}";
    private final static String TALK_ID = "1000";

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

	// test this
	public void testCreateTechDayTable() { // TODO name
		final PageHelper pageHelper = getPageHelper(idOfPageContainingChartMacro);

		assertTrue(pageHelper.read());
		
		pageHelper.setContent(MACROSTRING + 
				"\n| "+ TALK_ID +" | Name | Autor | TALK | | Anmerkung |\n"
				+ MACROSTRING);

		assertTrue(pageHelper.update());

		gotoPage("/pages/viewpage.action?pageId=" + pageHelper.getId());
		
		String interested = getElementAttributByXPath("//td[@id='audience."+TALK_ID+"']", "title");
        
		assertEquals("", interested);
		clickLink("techday."+TALK_ID);
	    assertTrue(pageHelper.update());
	    interested = getElementAttributByXPath("//td[@id='audience."+TALK_ID+"']", "title");
		assertEquals("admin", interested);
	}
	
	// TODO user full name. "admin" is just the login name
	// number of interested users
	// change type of line (coloring)
	
    //  + "| 201010221000 | Einführung und Praxisbericht über Apache Maven | [~winklerg] | TALK | | 1 Woche Vorlauf |"
    //  + "| 201010271500 | Erzeugen von Sprint- und Task-Zetteln aus Freemind | [~liebharc] | POINTER | Pointer | 1 Wochen"
}