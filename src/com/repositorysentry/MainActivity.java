package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {

	private EditText mUsername, mRepositoryName;

	// Notification ID to allow for future updates
	private static final int MY_NOTIFICATION_ID = 1;
	private long[] mVibratePattern = { 0, 200, 200, 300 };
	
	private AlarmManager mAlarmManager;
	private Intent mNotificationIntent;
	private PendingIntent mContentIntent;
	private static final long INITIAL_ALARM_DELAY = 5 * 60 * 1000L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		mNotificationIntent = new Intent(MainActivity.this, CommitNotificationReceiver.class);
		mContentIntent = PendingIntent.getBroadcast(MainActivity.this, 0, mNotificationIntent, 0);

		mUsername = (EditText) findViewById(R.id.username);
		mRepositoryName = (EditText) findViewById(R.id.repository);
		/*
		final Button buttonGetCommit = (Button) findViewById(R.id.get_commit_button);
		buttonGetCommit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String username = mUsername.getText().toString();
				String repoName = mRepositoryName.getText().toString();
				if (!username.isEmpty() && !repoName.isEmpty()) {
					ArrayList<HashMap<String, String>> commitsInfo = mCommit
							.getCommitsHistory(username, repoName);

					displayCommitsList(commitsInfo);

					ArrayList<HashMap<String, String>> newCommitsData = mCommit
							.getNewCommitsData(repoName);
					if (!newCommitsData.isEmpty()) {
						CharSequence contentText = "You've got " + newCommitsData.size()
								+ " new commits";
						CharSequence tickerText = "You've got new commits!";
						createCommitNotification(newCommitsData, contentText, tickerText);
					} else {
						CharSequence contentText = "You haven't got new commits.";
						CharSequence tickerText = "You haven't new commits!";
						createCommitNotification(newCommitsData, contentText, tickerText);
					}
				}
			}
		});*/
		
		final Button buttonSetAlarm = (Button) findViewById(R.id.set_alarm_button);
		buttonSetAlarm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 
						SystemClock.elapsedRealtime(), 
						INITIAL_ALARM_DELAY, 
						mContentIntent);
				
				Toast.makeText(getApplicationContext(),
						"Repository Sentry Set", Toast.LENGTH_LONG)
						.show();
				
			}
		});
		
		final Button buttonCancelRepeatingAlarm = (Button) findViewById(R.id.cancel_alarm_button);
		buttonCancelRepeatingAlarm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mAlarmManager.cancel(mContentIntent);
				
				Toast.makeText(getApplicationContext(),
						"Repository Sentry Cancelled", Toast.LENGTH_LONG).show();
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		//TODO: close database
		
		//mCommit.mDB.close();
		//mCommit.mDbHelper.deleteDatabase();

		super.onDestroy();

	}

	private void displayCommitsList(ArrayList<HashMap<String, String>> commitsInfo) {
		SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,
				(ArrayList<HashMap<String, String>>) commitsInfo.clone(),
				R.layout.list_item, new String[] { CommitHistory.NAME_TAG,
						CommitHistory.DATE_TAG, CommitHistory.MESSAGE_TAG },
				new int[] { R.id.commiterName, R.id.commitDate,
						R.id.commitMessage });

		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);
	}

	private void createCommitNotification(
			ArrayList<HashMap<String, String>> newCommitsData,
			CharSequence contentText, CharSequence tickerText) {
		CharSequence contentTitle = "New Commits";

		Notification.Builder notificationBuilder = new Notification.Builder(
				getApplicationContext()).setTicker(tickerText)
				.setSmallIcon(android.R.drawable.stat_sys_warning)
				.setAutoCancel(true)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setVibrate(mVibratePattern);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(MY_NOTIFICATION_ID,
				notificationBuilder.build());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
