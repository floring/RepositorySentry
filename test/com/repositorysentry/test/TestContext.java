package com.repositorysentry.test;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.test.mock.MockContext;

public class TestContext extends MockContext {
	private List<Intent> mReceivedIntents = new ArrayList<Intent>();

	@Override
	public String getPackageName() {
		return "com.repositorysentry.test";
	}

	public List<Intent> getReceivedIntents() {
		return mReceivedIntents;
	}
	
	public void addIntent(Intent intent) {
		mReceivedIntents.add(intent);
	}
}
