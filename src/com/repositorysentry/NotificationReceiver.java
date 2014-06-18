package com.repositorysentry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
	
	private static final String TAG = "CommitNotificationReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Repository repository = intent.getParcelableExtra(SentryCreator.INTENT_KEY_REPO);
		
		Log.i(TAG, "test: " + repository.getRepositoryName());
	}

}
