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
		int repoId = intent.getIntExtra("Id", 0);

		CommitInspector inspector = CommitInspector.getInstance();
		if (inspector.mDB == null) {
			inspector.mDbHelper = new DatabaseOpenHelper(context);
			inspector.mDB = inspector.mDbHelper.getWritableDatabase();
		} else {
			if (!inspector.mDB.isOpen()) {
				inspector.mDbHelper = new DatabaseOpenHelper(context);
				inspector.mDB = inspector.mDbHelper.getWritableDatabase();
			}
		}

		PoolRepositories pool = PoolRepositories.getInstance();
		if (pool.getSize() == 0) {
			// load repos from db to pool
			ArrayList<HashMap<String, String>> repoList = inspector.getRepositoriesFromDB();
			pool.loadPool(context, repoList);
		}
		Repository repository = pool.getRepository(repoId);
		ArrayList<HashMap<String, String>> commitsData = repository
				.getCommitsHistory();

		ArrayList<HashMap<String, String>> newCommitsData = inspector
				.getNewCommits(String.valueOf(repoId), repository.getType(),
						repository.getRepositoryName(), commitsData);

		if (!newCommitsData.isEmpty()) {
			mNotificationIntent = new Intent(context, MainActivity.class);
			mContentIntent = PendingIntent.getActivity(context, 0,
					mNotificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

			CharSequence contentText = "You've got " + newCommitsData.size()
					+ " new commits";
			CharSequence tickerText = "You've got new commits!";
			sendNotification(context, newCommitsData, contentText, tickerText,
					repository.getRepositoryName());
		}

		Log.i(TAG, "New commits count: " + newCommitsData.size()
				+ ". Sending commit notification at:"
				+ DateFormat.getDateTimeInstance().format(new Date()));
	}

	private void sendNotification(Context context,
			ArrayList<HashMap<String, String>> newCommitsData,
			CharSequence contentText, CharSequence tickerText,
			String repositoryName) {
		CharSequence contentTitle = repositoryName;

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
