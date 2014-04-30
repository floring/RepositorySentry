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
import java.util.Date;

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

	private static final int CREATE_ALARM_ITEM_REQUEST = 0;
	private static final String FILE_NAME = "AlarmsActivityData.txt";	
	
	private static long alarmInterval;

	private AlarmListAdapter mAlarmAdapter;
	private AlarmManager mAlarmManager;
	private GestureDetector mGestureDetector;
	private ListView mAlarmItems;
	private EditText etAlarmItemsFilter;

	private CommitInspector mInspector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_listview);

		mInspector = CommitInspector.getInstance();

		mInspector.mDbHelper = new DatabaseOpenHelper(this);
		mInspector.mDB = mInspector.mDbHelper.getWritableDatabase();

		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		mAlarmAdapter = new AlarmListAdapter(getApplicationContext());

		mAlarmItems = (ListView) findViewById(android.R.id.list);
		mAlarmItems.setAdapter(mAlarmAdapter);
		
		// TODO: Get and set default alarm interval 
		alarmInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

		// Calculate touch parameters based on display metrics
		DisplayMetrics dm = getResources().getDisplayMetrics();
		final int minDistance = (int) (120.0f * dm.densityDpi / 160.0f + 0.5);
		final int maxPath = (int) (250.0f * dm.densityDpi / 160.0f + 0.5);
		final double velocity = 200.0f * dm.densityDpi / 160.0f + 0.5;

		mGestureDetector = new GestureDetector(getApplicationContext(),
				new ListItemGestureDetector(minDistance, maxPath, velocity));
		mAlarmItems.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent event) {

				return mGestureDetector.onTouchEvent(event);
			}
		});

		/*final Button buttonCreateAlarm = (Button) findViewById(R.id.button_alarm_list_create);
		buttonCreateAlarm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(MainActivity.this,
						CreateAlarmActivity.class);
				startActivityForResult(intent, CREATE_ALARM_ITEM_REQUEST);
			}
		});*/

		etAlarmItemsFilter = (EditText) findViewById(R.id.edittext_alarm_listview_filter);
		etAlarmItemsFilter.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mAlarmAdapter.getFilter().filter(s);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				if(s.length() == 0) {
					mAlarmAdapter.saveItems();
				}
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == CREATE_ALARM_ITEM_REQUEST) {
			if (resultCode == RESULT_OK) {
				AlarmItem item = new AlarmItem(data);
				mAlarmAdapter.add(item);
			}
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		// Load saved AlarmItems, if necessary
		if (mAlarmAdapter.getCount() == 0) {

			/*
			 * File dir = getFilesDir(); File file = new File(dir, FILE_NAME);
			 * boolean deleted = file.delete();
			 */

			loadItems();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Save AlarmItems, unless they have been filtered
		if(etAlarmItemsFilter.getText().length() == 0) {
			saveItems();
		}
	}

	@Override
	protected void onDestroy() {
		if (mInspector.mDB != null) {
			if (mInspector.mDB.isOpen()) {
				mInspector.mDB.close();
			}
		}
		// mInspector.mDbHelper.deleteDatabase();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_create:
				Intent intent = new Intent(MainActivity.this,
						CreateAlarmActivity.class);
				intent.putExtra("Interval", alarmInterval);
				startActivityForResult(intent, CREATE_ALARM_ITEM_REQUEST);
				return true;
			case R.id.action_reset_all:
				mAlarmAdapter.clearAll();
				resetAllSentries();
				return true;
			case R.id.option_15_min:
				if(!item.isChecked()) {
					changeSentriesTriggerPeriod(AlarmManager.INTERVAL_FIFTEEN_MINUTES);
					item.setChecked(true);
				}
				return true;
			case R.id.option_30_min:
				if(!item.isChecked()) {
					changeSentriesTriggerPeriod(AlarmManager.INTERVAL_HALF_HOUR);
					item.setChecked(true);
				}
				return true;
			case R.id.option_1_hour:
				if(!item.isChecked()) {
					changeSentriesTriggerPeriod(AlarmManager.INTERVAL_HOUR);
					item.setChecked(true);
				}
				return true;
			case R.id.option_2_hour:
				if(!item.isChecked()) {
					changeSentriesTriggerPeriod(AlarmManager.INTERVAL_HOUR * 2);
					item.setChecked(true);
				}
				return true;
			case R.id.option_1_day:
				if(!item.isChecked()) {
					changeSentriesTriggerPeriod(AlarmManager.INTERVAL_DAY);
					item.setChecked(true);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void changeSentriesTriggerPeriod(final long interval) {
		if(!mAlarmAdapter.isEmpty()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					MainActivity.this);
			dialog.setTitle("Change triggering period");
			dialog.setMessage("Are you sure you want to change trigger period to all sentries?");
			dialog.setNegativeButton("Cancel", null);
			dialog.setPositiveButton("OK", new AlertDialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					alarmInterval = interval;
					for (int i = 0; i < mAlarmAdapter.getCount(); i++) {
						AlarmItem alarmItem = (AlarmItem) mAlarmAdapter
								.getItem(i);
						PendingIntent pendingNoteIntent = composeRequiredIntent(alarmItem);
						
						mAlarmManager.cancel(pendingNoteIntent);
						
						// TODO: change setRepeating to setInexactRepeating
						mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
								SystemClock.elapsedRealtime(), alarmInterval,
								pendingNoteIntent);
					}
				}
			});
			dialog.show();
		}		
	}

	private PendingIntent composeRequiredIntent(AlarmItem alarmItem) {
		Bundle bundle = new Bundle();
		bundle.putString("Username", alarmItem.getUsername());
		bundle.putString("RepositoryName", alarmItem.getRepositoryName());

		Intent notifIntent = new Intent(getApplicationContext(),
				NotificationReceiver.class);
		notifIntent.putExtras(bundle);
		PendingIntent pendingNoteIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, notifIntent, 0);

		return pendingNoteIntent;
	}

	private void resetAllSentries() {
		if (!mAlarmAdapter.isEmpty()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					MainActivity.this);
			dialog.setTitle("Delete sentries");
			dialog.setMessage("Are you sure you want to delete all sentries?");
			dialog.setNegativeButton("Cancel", null);
			dialog.setPositiveButton("OK", new AlertDialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface d, int which) {
					for (int i = 0; i < mAlarmAdapter.getCount(); i++) {
						AlarmItem alarmItem = (AlarmItem) mAlarmAdapter
								.getItem(i);
						PendingIntent pendingNoteIntent = composeRequiredIntent(alarmItem);

						mAlarmManager.cancel(pendingNoteIntent);
					}
					mAlarmAdapter.notifyDataSetChanged();

					Toast.makeText(getApplicationContext(),
							"All sentries has been deleted",
							Toast.LENGTH_SHORT).show();
				}
			});
			dialog.show();
		}
	}

	// Load stored AlarmItems
	private void loadItems() {
		BufferedReader reader = null;
		try {
			FileInputStream fis = openFileInput(FILE_NAME);
			reader = new BufferedReader(new InputStreamReader(fis));

			String username = null;
			String repository = null;
			Date date = null;

			while (null != (username = reader.readLine())) {
				repository = reader.readLine();
				date = AlarmItem.FORMAT.parse(reader.readLine());
				mAlarmAdapter.add(new AlarmItem(username, repository, date));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
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

	// Save AlarmItems to file
	private void saveItems() {
		PrintWriter writer = null;
		try {
			FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					fos)));

			for (int i = 0; i < mAlarmAdapter.getCount(); i++) {
				writer.println(mAlarmAdapter.getItem(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.close();
			}
		}
	}

	public class ListItemGestureDetector extends SimpleOnGestureListener {

		private int FLING_MIN_DISTANCE;
		private int FLING_MAX_OFF_PATH;
		private double FLING_THRESHOLD_VELOCITY;

		public ListItemGestureDetector(int minDistance, int maxPath,
				double velocity) {
			FLING_MIN_DISTANCE = minDistance;
			FLING_MAX_OFF_PATH = maxPath;
			FLING_THRESHOLD_VELOCITY = velocity;
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2,
				float velocityX, float velocityY) {
			if (Math.abs(event1.getY() - event2.getY()) > FLING_MAX_OFF_PATH) {
				return false;
			}
			// if: Right to Left fling
			// else: Left to Right fling
			if (event1.getX() - event2.getX() > FLING_MIN_DISTANCE
					&& Math.abs(velocityX) > FLING_THRESHOLD_VELOCITY) {

			} else if (event2.getX() - event1.getX() > FLING_MIN_DISTANCE
					&& Math.abs(velocityX) > FLING_THRESHOLD_VELOCITY) {
				int positionToRemove = mAlarmItems.pointToPosition(
						(int) event1.getX(), (int) event1.getY());

				AlarmItem alarmItem = (AlarmItem) mAlarmAdapter
						.getItem(positionToRemove);
				String repoName = alarmItem.getRepositoryName();
				PendingIntent pendingNoteIntent = composeRequiredIntent(alarmItem);
				mAlarmManager.cancel(pendingNoteIntent);

				mInspector.removeRepositoryRowsFromDB(repoName);

				mAlarmAdapter.removeItem(positionToRemove);
				mAlarmAdapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(),
						"Sentry to '" + repoName + "' has been cancelled",
						Toast.LENGTH_SHORT).show();
				
				return true;
			}
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			int itemPosition = mAlarmItems.pointToPosition((int) e.getX(),
					(int) e.getY());
			AlarmItem alarmItem = (AlarmItem) mAlarmAdapter
					.getItem(itemPosition);

			Bundle bundle = new Bundle();
			bundle.putString("Username", alarmItem.getUsername());
			bundle.putString("RepositoryName", alarmItem.getRepositoryName());
			Intent commitHistoryIntent = new Intent(getApplicationContext(),
					CommitHistoryActivity.class);
			commitHistoryIntent.putExtras(bundle);
			startActivity(commitHistoryIntent);

			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
	}

}
