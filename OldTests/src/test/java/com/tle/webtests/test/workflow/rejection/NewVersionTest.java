package com.tle.webtests.test.workflow.rejection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.openqa.selenium.Alert;
import org.openqa.selenium.UnhandledAlertException;
import org.testng.annotations.Test;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;


@TestInstitution("workflow")
public class NewVersionTest extends AbstractCleanupTest
{
	private static final String BACKTICK_PASSWORD = "``````";

	private static final String SORTING = "datemodified";

	private static final String ADMIN_USERNAME = "admin";

	private static final String FIRST_MODERATOR_USERNAME = "SimpleModerator";

	private static final String SECOND_MODERATOR_USERNAME = "SecondStepModerator";

	public NewVersionTest()
	{
		setDeleteCredentials(ADMIN_USERNAME, BACKTICK_PASSWORD );
	}

	@Test
	public void newVersion()
	{
		// Login as admin user and contribute an item to the collection
		logon(ADMIN_USERNAME, BACKTICK_PASSWORD);
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Move to Live During Moderation");
		String itemFullName = context.getFullName("item");
		String itemFullName2 = itemFullName + " 2";
		saveItem(wizard,1,itemFullName);
		logout();

		// Login as SimpleModerator and moderate the item at the first step.
		logon(FIRST_MODERATOR_USERNAME, BACKTICK_PASSWORD);
		doModeration( FIRST_MODERATOR_USERNAME, BACKTICK_PASSWORD, "moderation step 1", itemFullName, false );
		logout();

		//login as admin to validate that we can't create a new version while an item is still under moderation
		logon(ADMIN_USERNAME, BACKTICK_PASSWORD);
		Boolean alertDisplayed = false;
		try {
			SearchPage.searchAndView(context, itemFullName).adminTab().newVersion();
		}
		catch(UnhandledAlertException f){
			Alert alert = getContext().getDriver().switchTo().alert();
			alert.accept();
			alertDisplayed = true;
		}
		assertTrue( alertDisplayed, "Failed to trigger check of new version not allowed when already version in moderation.");
		logout();

		// Login as the SecondStepModerator and moderate again because this workflow needs moderating twice.
		logon(SECOND_MODERATOR_USERNAME, BACKTICK_PASSWORD);
		doModeration( SECOND_MODERATOR_USERNAME, BACKTICK_PASSWORD, "moderation step 2", itemFullName, false );
		logout();

		// Now the item should be in a state where we're allowed to create a new version.
		// login as admin and new version it.
		logon(ADMIN_USERNAME, BACKTICK_PASSWORD);
		wizard = SearchPage.searchAndView(context, itemFullName).adminTab().newVersion();
		saveItem(wizard,1,itemFullName2);
		// check that there is a version that is live, and one that is moderating.
		ItemListPage itemList = getItemList(itemFullName);
		assertEquals(itemList.getResultForTitle(itemFullName, 1).getStatus(), "live");
		assertEquals(itemList.getResultForTitle(itemFullName2, 1).getStatus(), "moderating");
		logout();

		// Login as SimpleModerator and moderate the new version of this item at the first step.
		logon(FIRST_MODERATOR_USERNAME, BACKTICK_PASSWORD);
		doModeration(FIRST_MODERATOR_USERNAME, BACKTICK_PASSWORD, "moderation step 1", itemFullName2,false);
		// After moderation, check that the item has gone life after the first moderation step
		itemList = getItemList(itemFullName2);
		assertEquals(itemList.getResultForTitle(itemFullName2, 1).getStatus(), "live");
		logout();

		// Login as the SecondStepModerator and check that the item is still
		// there to moderate, and is still live once moderated from second step.
		logon(SECOND_MODERATOR_USERNAME, BACKTICK_PASSWORD);
		doModeration(SECOND_MODERATOR_USERNAME,BACKTICK_PASSWORD,"", itemFullName2, true);
		logout();
	}

	private void doModeration( String username, String password, String stepName, String itemFullName, Boolean lastModeration ) {
		TaskListPage taskListPage = new TaskListPage(context).load();
		ModerateListSearchResults modResults = taskListPage.exactQuery(itemFullName);
		if(!lastModeration)
		{
			assertEquals(modResults.getStepName(itemFullName), stepName );
			modResults.moderate(itemFullName).accept();
		}
		else
		{
			ModerationView tasksTab = modResults.moderate(itemFullName);
			tasksTab.assignToMe();
			tasksTab.accept();
		}
	}

	private ItemListPage getItemList(String itemFullName){
		ItemAdminPage adminPage = new ItemAdminPage(context).load();
		adminPage.exactQuery(itemFullName);
		adminPage.setSort(SORTING);
		return adminPage.results();
	}

	private void saveItem(WizardPageTab wizard, int ctrlNum, String itemFullName){
		wizard.editbox(ctrlNum, itemFullName);
		wizard.save().submit();
	}
}
