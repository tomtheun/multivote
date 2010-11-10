package it.com.tngtech.confluence.techday;

import org.apache.commons.lang.StringUtils;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;
import com.atlassian.confluence.plugin.functest.helper.PageHelper;
import com.atlassian.confluence.plugin.functest.helper.SpaceHelper;

/**
 * Testing {@link com.tngtech.confluence.techday.TechdayMacro}
 */
public class TestTechdayMacro extends AbstractConfluencePluginWebTestCase {
	private long idOfPageContainingChartMacro;

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
	public void testCreateTechDayTable() {
		final PageHelper pageHelper = getPageHelper(idOfPageContainingChartMacro);

		System.out.println("bla");
		assertTrue(pageHelper.read());

		pageHelper.setContent( // TODO
				"| 201010141350 | Konferenzbericht Qt- Developer Days 2010 | [~pintarer] | TALK | | 1 Woche Vorlauf |"
					//	+ "| 201010221000 | Einführung und Praxisbericht über Apache Maven | [~winklerg] | TALK | | 1 Woche Vorlauf |"
					//	+ "| 201010271500 | Erzeugen von Sprint- und Task-Zetteln aus Freemind | [~liebharc] | POINTER | Pointer | 1 Wochen"
				);

		assertTrue(pageHelper.update());

		gotoPage("/pages/viewpage.action?pageId=" + pageHelper.getId());
		//pageHelper.

	}
}