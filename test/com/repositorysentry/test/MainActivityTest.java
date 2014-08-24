package com.repositorysentry.test;

import org.junit.Ignore;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.repositorysentry.MainActivity;
import com.repositorysentry.NotificationReceiver;
import com.repositorysentry.R;
import com.repositorysentry.Repository;

import com.robotium.solo.*;

public class MainActivityTest extends
		ActivityInstrumentationTestCase2<MainActivity> {

	private Solo solo;

	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation());
		getActivity();
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
		assertTrue("Activity stack not empty.",
				solo.waitForEmptyActivityStack(5000));
	}

	
	public void testCreateSentry() {
		int timeout = 10;

		// Wait for activity: 'com.repositorysentry.MainActivity'
		assertTrue("com.repositorysentry.MainActivity is not found!",
				solo.waitForActivity(com.repositorysentry.MainActivity.class,
						2000));
		// Click on '+'
		solo.clickOnView(solo.getView(R.id.action_create));

		// Wait for activity: 'com.repositorysentry.CreateSentryActivity'
		assertTrue(
				"com.repositorysentry.CreateSentryActivity is not found!",
				solo.waitForActivity(com.repositorysentry.CreateSentryActivity.class));
		
		// Set Username and RepositoryName parameters
		solo.clearEditText((android.widget.EditText) solo
				.getView(R.id.edittext_username));
		solo.enterText(
				(android.widget.EditText) solo.getView(R.id.edittext_username),
				"tUsername");
		solo.clearEditText((android.widget.EditText) solo
				.getView(R.id.edittext_repository));
		solo.enterText((android.widget.EditText) solo
				.getView(R.id.edittext_repository), "tRepoNameeee");
		// Set spinner value
		boolean actual = solo.searchText("Git");
		assertEquals("Spinner text not found", true, actual);
		solo.pressSpinnerItem(0, 1);
		actual = solo.isSpinnerTextSelected(0, "BitBucket");
		assertEquals("Spinner item BitBucket is not selected", true, actual);
		// Click on Set button
		solo.clickOnView(solo.getView(R.id.button_set_alarm));

		// Wait for activity: 'com.repositorysentry.MainActivity'
		assertTrue("com.repositorysentry.MainActivity is not found!",
				solo.waitForActivity(com.repositorysentry.MainActivity.class,
						2000));
		
		getActivity().dump();
		assertTrue(
				"Log message: 'Username:tUsername,Repository:tRepoName' not found",
				solo.waitForLogMessage(
						"Username:tUsername,Repository:tRepoName,Type:BitBucket",
						timeout));
		
		solo.sleep(2000);
		
		// Get the newest item from adapter
		ListAdapter adapter = ((ListView) getActivity().findViewById(android.R.id.list)).getAdapter();
		Repository item = (Repository) adapter.getItem(adapter.getCount() - 1);
		// Is alarm has set
		assertTrue("Alarm has not set", isAlarmSet(item.getRequestCode()));
		
		
	}
	
	public void testDeleteAll() {
		int timeout = 10;

		// Wait for activity: 'com.repositorysentry.MainActivity'
		assertTrue("com.repositorysentry.MainActivity is not found!",
				solo.waitForActivity(com.repositorysentry.MainActivity.class,
						2000));
		
		// click on Cancel button
		solo.clickOnView(solo.getView(R.id.action_reset_all));
		solo.waitForDialogToOpen(10000);
		solo.clickOnText("Cancel");
		getActivity().dump();
		assertTrue(
				"Log message: 'Username:tUsername,Repository:tRepoName' not found",
				solo.waitForLogMessage(
						"Username:tUsername,Repository:tRepoName,Type:BitBucket",
						timeout));
		
		// click on OK button
		solo.clickOnView(solo.getView(R.id.action_reset_all));
		solo.waitForDialogToOpen(10000);
		solo.clickOnText("OK");
		getActivity().dump();
		assertTrue(
				"Log message: Sentries have not been deleted",
				solo.waitForLogMessage(
						"There are no sentries",
						timeout));
	}
	
	
	public void testDelete() {
		assertTrue("com.repositorysentry.MainActivity is not found!",
				solo.waitForActivity(com.repositorysentry.MainActivity.class,
						2000));
		solo.sleep(1000);
		
		// Fling
		// Get the newest item from adapter
		ListAdapter adapter = ((ListView) getActivity().findViewById(android.R.id.list)).getAdapter();
		Repository repo = (Repository) adapter.getItem(adapter.getCount() - 1);
		int requestCode = repo.getRequestCode();
		
		ListView list = (ListView) solo.getView(android.R.id.list);
		RelativeLayout item = (RelativeLayout) list.getChildAt(list.getCount() - 1);
		TextView tv = (TextView) item.findViewById(R.id.textView_alarm_listview_item_repository);
		String text = tv.getText().toString();
		swipeRightOnText(text);
		solo.sleep(1000);
		
		getActivity().dump();
		assertTrue(
				"Log message: Sentry have not been deleted",
				solo.waitForLogMessage(
						"There are no sentries",
						10));
		
		// Is alarm has cancelled
		assertFalse("Alarm has set", isAlarmSet(requestCode));
	}	
	/*
	public void testOpenCommitHistory() {
		assertTrue("com.repositorysentry.MainActivity is not found!",
				solo.waitForActivity(com.repositorysentry.MainActivity.class,
						2000));
		solo.sleep(1000);
		
		// Tap
		solo.clickInList(0);
		assertTrue("com.repositorysentry.CommitHistoryActivity is not found!",
				solo.waitForActivity(com.repositorysentry.CommitHistoryActivity.class,
						2000));
	}*/
	
	protected void swipeRightOnText(String text) {
	    int fromX, toX, fromY, toY;
	    int[] location = new int[2];

	    View row = solo.getText(text);
	    row.getLocationInWindow(location);

	    // fail if the view with text cannot be located in the window
	    if (location.length == 0) {
	         fail("Could not find text: " + text);
	    }

	    fromX = location[0];
	    fromY = location[1];

	    toX = location[0] + 250;
	    toY = fromY;

	    solo.drag(fromX, toX, fromY, toY, 10);
	}
	
	protected boolean isAlarmSet(int requestCode) {
		Context context = this.getInstrumentation().getTargetContext().getApplicationContext(); 
		boolean alarmUp = (PendingIntent.getBroadcast(context, requestCode, 
		        new Intent(context,NotificationReceiver.class), 
		        PendingIntent.FLAG_NO_CREATE) != null);
		return alarmUp;
	}

}
