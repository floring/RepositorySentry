package com.repositorysentry.test;

import com.robotium.solo.*;

import com.repositorysentry.CreateSentryActivity;
import com.repositorysentry.R;

import android.test.ActivityInstrumentationTestCase2;

public class CreateSentryActivityTest extends
		ActivityInstrumentationTestCase2<CreateSentryActivity> {
	private Solo solo;

	public CreateSentryActivityTest() {
		super(CreateSentryActivity.class);
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

	public void testClearButton() {
		solo.clearEditText((android.widget.EditText) solo
				.getView(R.id.edittext_username));
		solo.enterText(
				(android.widget.EditText) solo.getView(R.id.edittext_username),
				"test");
		solo.clickOnView(solo.getView(com.repositorysentry.R.id.button_clear));
		solo.getView(R.id.edittext_username);
		assertTrue("EditText Username is not empty", solo.waitForText(""));

	}
}
