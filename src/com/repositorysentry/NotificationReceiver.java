package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Date;
import java.text.DateFormat;

public class NotificationReceiver extends BroadcastReceiver {
	
	private static final String TAG = "CommitNotificationReceiver";
	
	private PendingIntent mContentIntent;
	
	private static final int COMMIT_NOTIFICATION_ID = 1;
	private static final long[] VIBRATE_PATTERN = { 0, 200, 200, 300 };

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Repository repository = intent.getParcelableExtra(SentryCreator.INTENT_KEY_REPO);
		ArrayList<HashMap<String, String>> commits = repository.getCommits();
		Intent notificationIntent = new Intent(context, MainActivity.class);
		mContentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		if(commits != null) {
			CommitInspector inspector = CommitInspector.getInstance();
			if(inspector.DB == null || !inspector.DB.isOpen()) {
				inspector.DbHelper = new DatabaseOpenHelper(context);
				inspector.DB = inspector.DbHelper.getWritableDatabase();
			}
			ArrayList<HashMap<String, String>> newCommitsData = inspector
					.getNewCommits(repository, commits);
			
			if (!newCommitsData.isEmpty()) {
				CharSequence tickerText = "You've got new commits!";
				CharSequence contentTitle = repository.getRepositoryName();
				CharSequence contentText = "You've got " + newCommitsData.size()
						+ " new commits";
				sendNotification(context, tickerText, contentTitle, contentText);
			}
		}
		else {
			CharSequence tickerText = context.getResources().getText(R.string.app_name) + " report";
			CharSequence contentTitle = "Failed to obtain commits";
			CharSequence contentText = "Please, check the data is spelled correctly.";
			sendNotification(context, tickerText, contentTitle, contentText);
		}

	
		
		Log.i(TAG, "test: " + repository.getRepositoryName() + ". Sending commit notification at:"
				+ DateFormat.getDateTimeInstance().format(new Date()));
	}
	
	private void sendNotification(Context context, CharSequence tickerText, CharSequence contentTitle, CharSequence contentText) {		
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder notificationBuilder = new Notification.Builder(
				context).setTicker(tickerText).setContentTitle(contentTitle)
				.setContentText(contentText).setAutoCancel(true)
				.setSmallIcon(android.R.drawable.stat_sys_warning)
				.setVibrate(VIBRATE_PATTERN).setContentIntent(mContentIntent);
		notificationManager.notify(COMMIT_NOTIFICATION_ID,
				notificationBuilder.build());
	}

}
