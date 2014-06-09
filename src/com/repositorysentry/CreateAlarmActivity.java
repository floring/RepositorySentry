package com.repositorysentry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class CreateAlarmActivity extends Activity {

	private EditText mUsernameText;
	private EditText mRepositoryText;
	private Spinner mSpinnerVcs;
	
	public static long ALARM_DELAY;
	public static final long INITIAL_ALARM_DELAY = 15 * 60 * 1000L;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_alarm);
		
		mSpinnerVcs = (Spinner) findViewById(R.id.spinnerVcs);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.vcs, R.layout.vsc_dropdown_item);
		mSpinnerVcs.setAdapter(adapter);
		mSpinnerVcs.setSelection(0);

		mUsernameText = (EditText) findViewById(R.id.edittext_username);
		mRepositoryText = (EditText) findViewById(R.id.edittext_repository);

		ALARM_DELAY = getIntent().getLongExtra("Interval", MainActivity.DEFAULT_ALARM_DELAY);
		

		// OnClickListener for Clear All Button
		final Button buttonClear = (Button) findViewById(R.id.button_clear);
		buttonClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mUsernameText.setText("");
				mRepositoryText.setText("");
			}
		});

		// OnClickListener for Set Alarm Button
		final Button buttonSetAlarm = (Button) findViewById(R.id.button_set_alarm);
		buttonSetAlarm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int id = PoolRepositories.ID;
				String username = mUsernameText.getText().toString();
				String repositoryName = mRepositoryText.getText().toString();

				createAlarmItem(id, username, repositoryName);
				createRepositoryItem(id, username, repositoryName);

				finish();
			}
		});

	}

	private void createAlarmItem(int id, String username, String repositoryName) {
		String creationDate = getDateString();
		Intent data = new Intent();
		AlarmItem.packageIntent(data, username, repositoryName, creationDate, id);
		setResult(RESULT_OK, data);
	};
	
	private void createRepositoryItem(int id, String username, String repositoryName) {
		Repository repository = null;
		switch(mSpinnerVcs.getSelectedItemPosition()) {
			case 0:
				repository = new GitRepository(getApplicationContext(), id, username, repositoryName);
				break;
			case 1:
				repository = new BitbucketRepository(getApplicationContext(), id, username, repositoryName);
				break;
		}
		PoolRepositories pool = PoolRepositories.getInstance();
		pool.add(repository);
		
		//repository.setAlarm();
		setAlarm(repository);
		
		PoolRepositories.ID++;
	}
	
	private void setAlarm(Repository repository) {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Bundle bundle = new Bundle();
		bundle.putString("RepositoryId", String.valueOf(repository.getRepoId()));
		Intent notificationIntent = new Intent(getApplicationContext(),
				NotificationReceiver.class);
		notificationIntent.putExtras(bundle);

		PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
				notificationIntent, 0);

		// TODO: change 3nd parameter to CreateAlarmActivity.ALARM_DELAY
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime(),
				CreateAlarmActivity.INITIAL_ALARM_DELAY, contentIntent);

		Toast.makeText(getApplicationContext(), "Repository Sentry Set", Toast.LENGTH_LONG)
				.show();

	}


	private String getDateString() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int monthOfYear = c.get(Calendar.MONTH);
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

		// Increment monthOfYear for Calendar/Date -> Time Format setting
		monthOfYear++;
		String mon = "" + monthOfYear;
		String day = "" + dayOfMonth;

		if (monthOfYear < 10)
			mon = "0" + monthOfYear;
		if (dayOfMonth < 10)
			day = "0" + dayOfMonth;

		String dateString = day + "." + mon + "." + year;

		return dateString;
	}

}
