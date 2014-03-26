package com.repositorysentry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.text.DateFormat;
import java.util.Date;

public class CommitNotificationReceiver extends BroadcastReceiver {
	
	private static final String TAG = "CommitNotificationReceiver";
	
	private Intent mNotificationIntent;
	private PendingIntent mContentIntent;
	
	CharSequence tickerText = "You've got new commits!";
	CharSequence contentTitle = "New Commits";
	CharSequence contentText = "You've got new commits";
	private static final int COMMIT_NOTIFICATION_ID = 1;
	private long[] mVibratePattern = { 0, 200, 200, 300 };


	@Override
	public void onReceive(Context context, Intent intent) {
		mNotificationIntent = new Intent(context, MainActivity.class);
		mContentIntent = PendingIntent.getActivity(context, 0, mNotificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder notificationBuilder = new Notification.Builder(context)
				.setTicker(tickerText)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setAutoCancel(true)
				.setSmallIcon(android.R.drawable.stat_sys_warning)
				.setVibrate(mVibratePattern)
				.setContentIntent(mContentIntent);
		notificationManager.notify(COMMIT_NOTIFICATION_ID, notificationBuilder.build());
		
		Log.i(TAG,"Sending notification at:" + DateFormat.getDateTimeInstance().format(new Date()));
	}

}
