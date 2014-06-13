package com.repositorysentry;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class SentryCreator {

	private static String mRepoType;
	private static String mUsername;
	private static String mRepositoryName;
	private Context mContext;

	public SentryCreator(Context context, String repoType, String username,
			String repoName) {
		mRepoType = repoType;
		mUsername = username;
		mRepositoryName = repoName;
		mContext = context;
	}

	public void create() {
		Repository repo = createRepository(mContext, mRepoType, mUsername,
				mRepositoryName);

		setSentry(repo);
	}
	
	public void remove() {
		
	}

	private Repository createRepository(Context context, String repoType,
			String username, String repoName) {
		Repository repository = null;
		if (repoType.equals("Git")) {
			repository = new GitRepository(context, username, repoName);
		} else if (repoType.equals("BitBucket")) {
			repository = new BitbucketRepository(context, username, repoName);
		}
		return repository;
	}

	private void setSentry(Repository repository) {
		PendingIntent contentIntent = composePendingIntent(repository);
		
		AlarmManager alarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);

		// TODO: change 3nd parameter to CreateAlarmActivity.ALARM_DELAY
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime(),
				CreateSentryActivity.INITIAL_ALARM_DELAY, contentIntent);
	}
	
	private void removeSentry(Repository repository) {
		PendingIntent contentIntent = composePendingIntent(repository);
		AlarmManager alarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(contentIntent);
	}
	
	private PendingIntent composePendingIntent(Repository repository) {		
		Intent notificationIntent = new Intent(mContext,
				NotificationReceiver.class);
		notificationIntent.putExtra("Repository", repository);
		PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0,
				notificationIntent, 0);

		return contentIntent;
	}
}
