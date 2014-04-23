package com.repositorysentry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class NotificationReceiver extends BroadcastReceiver {

	private static final String TAG = "CommitNotificationReceiver";
	
	private Intent mNotificationIntent;
	private PendingIntent mContentIntent;

	private static final int COMMIT_NOTIFICATION_ID = 1;
	private long[] mVibratePattern = { 0, 200, 200, 300 };

	@Override
	public void onReceive(Context context, Intent intent) {
		String username = intent.getStringExtra("Username");
		String repositoryName = intent.getStringExtra("RepositoryName");
		
		mNotificationIntent = new Intent(context, MainActivity.class);
		mContentIntent = PendingIntent.getActivity(context, 0,
				mNotificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		CommitHistory commitHistory = new CommitHistory(context);
		ArrayList<HashMap<String, String>> commitsInfo = commitHistory
				.getCommitsHistory(username, repositoryName);
		
		CommitInspector inspector = CommitInspector.getInstance();
		ArrayList<HashMap<String, String>> newCommitsData = inspector
				.getNewCommits(repositoryName, commitsInfo);

		if (!newCommitsData.isEmpty()) {
			CharSequence contentText = "You've got " + newCommitsData.size()
					+ " new commits";
			CharSequence tickerText = "You've got new commits!";
			sendNotification(context, newCommitsData, contentText, tickerText);
		}
		
		Log.i(TAG, "Sending commit notification at:"
				+ DateFormat.getDateTimeInstance().format(new Date()));
	}

	private void sendNotification(Context context,
			ArrayList<HashMap<String, String>> newCommitsData,
			CharSequence contentText, CharSequence tickerText) {
		CharSequence contentTitle = "New Commits";

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder notificationBuilder = new Notification.Builder(
				context).setTicker(tickerText).setContentTitle(contentTitle)
				.setContentText(contentText).setAutoCancel(true)
				.setSmallIcon(android.R.drawable.stat_sys_warning)
				.setVibrate(mVibratePattern).setContentIntent(mContentIntent);
		notificationManager.notify(COMMIT_NOTIFICATION_ID,
				notificationBuilder.build());
	}
}
