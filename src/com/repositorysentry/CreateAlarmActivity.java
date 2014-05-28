package com.repositorysentry;

import java.util.Calendar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
	private static long ALARM_DELAY;

	private AlarmManager mAlarmManager;
	private Intent mNotificationIntent;
	private PendingIntent mContentIntent;
	private static final long INITIAL_ALARM_DELAY = 5 * 60 * 1000L;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_alarm);

		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

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
				String username = mUsernameText.getText().toString();
				String repositoryName = mRepositoryText.getText().toString();

				createAlarmItem(username, repositoryName);
				setAlarm(username, repositoryName);

				finish();
			}
		});

	}

	private void createAlarmItem(String username, String repositoryName) {
		String creationDate = getDateString();
		Intent data = new Intent();
		AlarmItem.packageIntent(data, username, repositoryName, creationDate);
		setResult(RESULT_OK, data);
	};

	private void setAlarm(String username, String repositoryName) {

		Bundle bundle = new Bundle();
		bundle.putString("Username", username);
		bundle.putString("RepositoryName", repositoryName);
		mNotificationIntent = new Intent(getApplicationContext(),
				NotificationReceiver.class);
		mNotificationIntent.putExtras(bundle);

		mContentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
				mNotificationIntent, 0);

		// TODO: change 2nd parameter to SystemClock.elapsedRealtime() +
		// INITIAL_ALARM_DELAY
		//mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
			//	SystemClock.elapsedRealtime(), INITIAL_ALARM_DELAY,
				//mContentIntent);

		mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime(), INITIAL_ALARM_DELAY, mContentIntent);

		Toast.makeText(getApplicationContext(), "Repository Sentry Set",
				Toast.LENGTH_LONG).show();
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
