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
				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		ArrayList<HashMap<String, String>> newCommitsData = new ArrayList<HashMap<String, String>>();
		if(commits != null) {
			CommitInspector inspector = CommitInspector.getInstance();
			if(inspector.DB == null || !inspector.DB.isOpen()) {
				inspector.DbHelper = new DatabaseOpenHelper(context);
				inspector.DB = inspector.DbHelper.getWritableDatabase();
			}
			newCommitsData = inspector
					.getNewCommits(repository, commits);
			
			if (!newCommitsData.isEmpty()) {
				CharSequence tickerText = context.getResources().getText(R.string.you_get_new_commits);
				CharSequence contentTitle = repository.getRepositoryName();
				CharSequence contentText = context.getResources().getString(R.string.new_commits_number) + newCommitsData.size();
				sendNotification(context, tickerText, contentTitle, contentText);
			}
		}
		else {
			//CharSequence tickerText = context.getResources().getString(R.string.app_name) + context.getResources().getString(R.string.report);
			//CharSequence contentTitle = repository.getRepositoryName() + context.getResources().getString(R.string.fail_notif_title);
			CharSequence tickerText = context.getResources().getString(R.string.fail_notif_title);
			CharSequence contentTitle = repository.getRepositoryName();
			CharSequence contentText = context.getResources().getString(R.string.fail_notif_msg);
			sendNotification(context, tickerText, contentTitle, contentText);
		}	
		
		Log.i(TAG, "New commits: " + newCommitsData.size() +  ". " + repository.getRepositoryName() + ". Sending commit notification at:"
				+ DateFormat.getDateTimeInstance().format(new Date()));
				
		/*Log.i(TAG, "New commits: " + 256 +  ". " + "lala" + ". Sending commit notification at:"
				+ DateFormat.getDateTimeInstance().format(new Date()));*/
	}

	
	private void sendNotification(Context context, CharSequence tickerText, CharSequence contentTitle, CharSequence contentText) {		
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder notificationBuilder = new Notification.Builder(
				context).setTicker(tickerText).setContentTitle(contentTitle)
				.setContentText(contentText).setAutoCancel(true)
				.setSmallIcon(R.drawable.ic_launcher)
				.setVibrate(VIBRATE_PATTERN).setContentIntent(mContentIntent);
		notificationManager.notify(COMMIT_NOTIFICATION_ID,
				notificationBuilder.build());
	}

}
