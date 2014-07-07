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
	private Repository mRepository;
	
	private static final long INITIAL_ALARM_DELAY = 1 * 60 * 1000L;
	public static final String INTENT_KEY_REPO = "Repository";

	public SentryCreator(Context context, String repoType, String username,
			String repoName) {
		mRepoType = repoType;
		mUsername = username;
		mRepositoryName = repoName;
		mContext = context;
	}
	
	public SentryCreator(Context context, Repository r) {
		mContext = context;
		mRepository = r;
	}

	public Repository create() {
		Repository repo = createRepository(mContext, mRepoType, mUsername,
				mRepositoryName);
		setSentry(repo);
		
		return repo;
	}
	
	public void remove() {
		removeSentry(mRepository);
	}
	
	public void changeInterval() {
		changeSentryInterval(mRepository);
	}

	private Repository createRepository(Context context, String repoType,
			String username, String repoName) {
		Repository repository = null;
		if (repoType.equals(Vcs.Git.toString())) {
			repository = new GitRepository(context, username, repoName);
		} else if (repoType.equals(Vcs.BitBucket.toString())) {
			repository = new BitbucketRepository(context, username, repoName);
		}
		return repository;
	}

	private void setSentry(Repository repository) {
		PendingIntent contentIntent = composePendingIntent(repository);
		
		AlarmManager alarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);

		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + INITIAL_ALARM_DELAY,
				MainActivity.getAlarmInterval(), contentIntent);
	}
	
	private void removeSentry(Repository repository) {
		PendingIntent contentIntent = composePendingIntent(repository);
		AlarmManager alarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(contentIntent);
	}
	
	private void changeSentryInterval(Repository repository) {
		removeSentry(repository);
		setSentry(repository);
	}
	
	private PendingIntent composePendingIntent(Repository repository) {	
		int requestCode = repository.getRequestCode();
		
		Intent notificationIntent = new Intent(mContext,
				NotificationReceiver.class);		
		notificationIntent.putExtra(INTENT_KEY_REPO, repository);
		PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, requestCode,
				notificationIntent, 0);

		return contentIntent;
	}
}
