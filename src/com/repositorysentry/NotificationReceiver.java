package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Date;
import java.text.DateFormat;

public class NotificationReceiver extends BroadcastReceiver {
	
	private static final String TAG = "CommitNotificationReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Repository repository = intent.getParcelableExtra(SentryCreator.INTENT_KEY_REPO);
		/*ArrayList<HashMap<String, String>> commits = repository.getCommitsHistory();
		if(commits != null) {
			CommitInspector inspector = CommitInspector.getInstance();
			if(inspector.DB == null || !inspector.DB.isOpen()) {
				inspector.DbHelper = new DatabaseOpenHelper(context);
				inspector.DB = inspector.DbHelper.getWritableDatabase();
			}
			ArrayList<HashMap<String, String>> newCommitsData = inspector
					.getNewCommits(repository, commits);
		}
		else {
			//send zero notification
		}*/

	
		
		Log.i(TAG, "test: " + repository.getRepositoryName() + ". Sending commit notification at:"
				+ DateFormat.getDateTimeInstance().format(new Date()));
	}

}
