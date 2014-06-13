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

public class CreateSentryActivity extends Activity {

	private EditText mUsernameText;
	private EditText mRepositoryText;
	private Spinner mSpinnerVcs;
	
	private static int DEFAULT_REPO_TYPE = 0; 
	
	public static long ALARM_DELAY;
	public static final long INITIAL_ALARM_DELAY = 15 * 60 * 1000L;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_alarm);
		
		mSpinnerVcs = (Spinner) findViewById(R.id.spinnerVcs);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.vcs, R.layout.vsc_dropdown_item);
		mSpinnerVcs.setAdapter(adapter);
		mSpinnerVcs.setSelection(DEFAULT_REPO_TYPE);

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
				String repoType = mSpinnerVcs.getSelectedItem().toString();
				
				SentryCreator sentryCreator = new SentryCreator(getApplicationContext(), repoType, username, repositoryName);
				sentryCreator.create();
				
				finish();
			}
		});

	}
}
