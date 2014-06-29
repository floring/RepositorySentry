package com.repositorysentry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final long DEFAULT_ALARM_DELAY = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

	private static final int CREATE_ALARM_ITEM_REQUEST = 0;
	private static final String APP_SETTINGS = "RepoSentryPrefsFile";
	private static final String FILE_SENTRIES = "SentriesData.txt";

	private static long alarmInterval;

	private SentryItemAdapter mSentryAdapter;
	private ListView mSentryItems;
	private EditText mEtItemsFilter;
	private CommitInspector mInspector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_listview);
		
		mInspector = CommitInspector.getInstance();
		if(mInspector.DB == null || !mInspector.DB.isOpen()) {
			mInspector.DbHelper = new DatabaseOpenHelper(getApplicationContext());
			mInspector.DB = mInspector.DbHelper.getWritableDatabase();
		}

		mSentryAdapter = new SentryItemAdapter(getApplicationContext());
		mSentryItems = (ListView) findViewById(android.R.id.list);
		mSentryItems.setAdapter(mSentryAdapter);

		// Restore preferences
		SharedPreferences settings = getSharedPreferences(APP_SETTINGS,
				MODE_PRIVATE);
		alarmInterval = settings.getLong("alarmInterval", DEFAULT_ALARM_DELAY);

		// Calculate touch parameters based on display metrics
		DisplayMetrics dm = getResources().getDisplayMetrics();
		final int minDistance = (int) (120.0f * dm.densityDpi / 160.0f + 0.5);
		final int maxPath = (int) (250.0f * dm.densityDpi / 160.0f + 0.5);
		final double velocity = 200.0f * dm.densityDpi / 160.0f + 0.5;

		final GestureDetector gestureDetector = new GestureDetector(
				getApplicationContext(), new ListGestureDetector(
						getApplicationContext(), mSentryAdapter, mSentryItems,
						minDistance, maxPath, velocity));
		mSentryItems.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent event) {

				return gestureDetector.onTouchEvent(event);
			}
		});
		
		mEtItemsFilter = (EditText) findViewById(R.id.edittext_alarm_listview_filter);
		mEtItemsFilter.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mSentryAdapter.getFilter().filter(s);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				if (s.length() == 0) {
					mSentryAdapter.saveItems();
				}
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == CREATE_ALARM_ITEM_REQUEST) {
			if (resultCode == RESULT_OK) {
				mSentryAdapter
						.add((Repository) data
								.getParcelableExtra(CreateSentryActivity.INTENT_KEY_SENTRY));
			}
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		// Load saved SentryItems, if necessary
		if (mSentryAdapter.getCount() == 0) {
			loadItems();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// Save SentryItems, unless they have been filtered
		if (mEtItemsFilter.getText().length() == 0) {
			saveItems();
		}
	}

	@Override
	protected void onDestroy() {

		// Commit application preferences
		SharedPreferences settings = getSharedPreferences(APP_SETTINGS,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("alarmInterval", alarmInterval);
		editor.commit();
		
		// Close database
		if (mInspector.DB != null) {
			if (mInspector.DB.isOpen()) {
				mInspector.DB.close();
			}
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		if (alarmInterval == AlarmManager.INTERVAL_FIFTEEN_MINUTES)
			menu.findItem(R.id.option_15_min).setChecked(true);
		else if (alarmInterval == AlarmManager.INTERVAL_HALF_HOUR)
			menu.findItem(R.id.option_30_min).setChecked(true);
		else if (alarmInterval == AlarmManager.INTERVAL_HOUR)
			menu.findItem(R.id.option_1_hour).setChecked(true);
		else if (alarmInterval == AlarmManager.INTERVAL_HOUR * 2)
			menu.findItem(R.id.option_2_hour).setChecked(true);
		else if (alarmInterval == AlarmManager.INTERVAL_DAY)
			menu.findItem(R.id.option_1_day).setChecked(true);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_create:
			Intent intent = new Intent(MainActivity.this,
					CreateSentryActivity.class);
			intent.putExtra("Interval", alarmInterval);
			startActivityForResult(intent, CREATE_ALARM_ITEM_REQUEST);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/** Load stored Sentry Items */
	private void loadItems() {
		BufferedReader reader = null;
		try {
			FileInputStream fis = openFileInput(FILE_SENTRIES);
			reader = new BufferedReader(new InputStreamReader(fis));

			String repoId = null, username = null, repoName = null, date = null, type = null;

			while (null != (repoId = reader.readLine())) {
				username = reader.readLine();
				repoName = reader.readLine();
				date = reader.readLine();
				type = reader.readLine();

				Repository repository = null;
				if (type.equals(Vcs.Git.toString())) {
					repository = new GitRepository(UUID.fromString(repoId),
							getApplicationContext(), username, repoName, date);
				} else if (type.equals(Vcs.BitBucket.toString())) {
					repository = new BitbucketRepository(
							UUID.fromString(repoId), getApplicationContext(),
							username, repoName, date);
				}
				mSentryAdapter.add(repository);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** Save SentryItems to file */
	private void saveItems() {
		PrintWriter writer = null;
		try {
			FileOutputStream fos = openFileOutput(FILE_SENTRIES, MODE_PRIVATE);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					fos)));

			for (int i = 0; i < mSentryAdapter.getCount(); i++) {
				writer.println(mSentryAdapter.getItem(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.close();
			}
		}
	}
}
