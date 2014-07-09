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
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final long DEFAULT_ALARM_DELAY = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

	private static final int CREATE_ALARM_ITEM_REQUEST = 0;
	private static final String APP_SETTINGS = "RepoSentryPrefsFile";
	private static final String FILE_SENTRIES = "SentriesData.txt";

	// private static long ALARM_INTERVAL = 5 * 60 * 1000L;
	private static long ALARM_INTERVAL;

	private SentryItemAdapter mSentryAdapter;
	private ListView mSentryItems;
	private EditText mEtItemsFilter;
	private CommitInspector mInspector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_listview);

		mInspector = CommitInspector.getInstance();
		if (mInspector.DB == null || !mInspector.DB.isOpen()) {
			mInspector.DbHelper = new DatabaseOpenHelper(
					getApplicationContext());
			mInspector.DB = mInspector.DbHelper.getWritableDatabase();
		}

		mSentryAdapter = new SentryItemAdapter(getApplicationContext());
		mSentryItems = (ListView) findViewById(android.R.id.list);
		mSentryItems.setAdapter(mSentryAdapter);

		// Restore preferences
		SharedPreferences settings = getSharedPreferences(APP_SETTINGS,
				MODE_PRIVATE);
		setAlarmInterval(settings.getLong("alarmInterval", DEFAULT_ALARM_DELAY));

		// Calculate touch parameters based on display metrics
		DisplayMetrics dm = getResources().getDisplayMetrics();
		final int minDistance = (int) (120.0f * dm.densityDpi / 160.0f + 0.5);
		final int maxPath = (int) (250.0f * dm.densityDpi / 160.0f + 0.5);
		final double velocity = 200.0f * dm.densityDpi / 160.0f + 0.5;

		final GestureDetector gestureDetector = new GestureDetector(
				getApplicationContext(), new ListGestureDetector(minDistance,
						maxPath, velocity));
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
		editor.putLong("alarmInterval", getAlarmInterval());
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

		if (ALARM_INTERVAL == AlarmManager.INTERVAL_FIFTEEN_MINUTES)
			menu.findItem(R.id.option_15_min).setChecked(true);
		else if (ALARM_INTERVAL == AlarmManager.INTERVAL_HALF_HOUR)
			menu.findItem(R.id.option_30_min).setChecked(true);
		else if (ALARM_INTERVAL == AlarmManager.INTERVAL_HOUR)
			menu.findItem(R.id.option_1_hour).setChecked(true);
		else if (ALARM_INTERVAL == AlarmManager.INTERVAL_HOUR * 2)
			menu.findItem(R.id.option_2_hour).setChecked(true);
		else if (ALARM_INTERVAL == AlarmManager.INTERVAL_DAY)
			menu.findItem(R.id.option_1_day).setChecked(true);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_create:
			Intent intent = new Intent(MainActivity.this,
					CreateSentryActivity.class);
			startActivityForResult(intent, CREATE_ALARM_ITEM_REQUEST);
			break;
		case R.id.action_reset_all:
			deleteAll();
			break;
		case R.id.option_15_min:
			if (!item.isChecked()) {
				changeFiringPeriod(AlarmManager.INTERVAL_FIFTEEN_MINUTES, item);
			}
			break;
		case R.id.option_30_min:
			if (!item.isChecked()) {
				changeFiringPeriod(AlarmManager.INTERVAL_HALF_HOUR, item);
			}
			break;
		case R.id.option_1_hour:
			if (!item.isChecked()) {
				changeFiringPeriod(AlarmManager.INTERVAL_HOUR, item);
			}
			break;
		case R.id.option_2_hour:
			if (!item.isChecked()) {
				changeFiringPeriod(AlarmManager.INTERVAL_HOUR * 2, item);
			}
			break;
		case R.id.option_1_day:
			if (!item.isChecked()) {
				changeFiringPeriod(AlarmManager.INTERVAL_DAY, item);
			}
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	/** Load stored SentryItems */
	private void loadItems() {
		BufferedReader reader = null;
		try {
			FileInputStream fis = openFileInput(FILE_SENTRIES);
			reader = new BufferedReader(new InputStreamReader(fis));

			String repoId = null, username = null, repoName = null, date = null, type = null, code = null;

			while (null != (repoId = reader.readLine())) {
				username = reader.readLine();
				repoName = reader.readLine();
				date = reader.readLine();
				type = reader.readLine();
				code = reader.readLine();

				Repository repository = null;
				if (type.equals(Vcs.Git.toString())) {
					repository = new GitRepository(UUID.fromString(repoId),
							getApplicationContext(), username, repoName, date,
							Integer.parseInt(code));
				} else if (type.equals(Vcs.BitBucket.toString())) {
					repository = new BitbucketRepository(
							UUID.fromString(repoId), getApplicationContext(),
							username, repoName, date, Integer.parseInt(code));
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

	/** Delete Sentry item */
	private void deleteItem(int position) {
		Repository repoItem = (Repository) mSentryAdapter.getItem(position);
		SentryCreator creator = new SentryCreator(getApplicationContext(),
				repoItem);
		creator.remove();

		mInspector.remove(repoItem);
		mSentryAdapter.removeItem(position);
	}

	/** Delete all sentries */
	private void deleteAll() {
		if (!mSentryAdapter.isEmpty()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					MainActivity.this);
			dialog.setTitle(R.string.dialog_title_delete_all);
			dialog.setMessage(R.string.dialog_msg_delete_all);
			dialog.setNegativeButton(R.string.dialog_cancel, null);
			dialog.setPositiveButton(R.string.dialog_ok,
					new AlertDialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface d, int which) {
							for (int i = 0; i < mSentryAdapter.getCount(); i++) {
								deleteItem(i);
							}
							mSentryAdapter.notifyDataSetChanged();

							Toast.makeText(getApplicationContext(),
									R.string.toast_delete_all,
									Toast.LENGTH_SHORT).show();
						}
					});
			dialog.show();
		}
	}

	/** Change the firing period of all sentries */
	private void changeFiringPeriod(final long interval, final MenuItem item) {
		if (!mSentryAdapter.isEmpty()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					MainActivity.this);
			dialog.setTitle(R.string.dialog_title_change_period);
			dialog.setMessage(R.string.dialog_msg_change_period);
			dialog.setNegativeButton(R.string.dialog_cancel, null);
			dialog.setPositiveButton(R.string.dialog_ok,
					new AlertDialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							setAlarmInterval(interval);
							for (int i = 0; i < mSentryAdapter.getCount(); i++) {
								/*
								 * Repository repoItem = (Repository)
								 * mSentryAdapter .getItem(i);
								 * 
								 * SentryCreator creator = new
								 * SentryCreator(getApplicationContext(),
								 * repoItem); creator.changeInterval();
								 */
								Repository repoItem = (Repository) mSentryAdapter
										.getItem(i);
								String repoType = repoItem.getType();
								String username = repoItem.getUsername();
								String repoName = repoItem.getRepositoryName();

								SentryCreator creator = new SentryCreator(
										getApplicationContext(), repoItem);
								creator.remove();
								mSentryAdapter.removeItem(i);
								
								creator = new SentryCreator(getApplicationContext(), repoType, username, repoName);
								Repository repo = creator.create();
								mSentryAdapter.add(repo);
							}
							item.setChecked(true);
							mSentryAdapter.notifyDataSetChanged();
						}
					});
			dialog.show();
		}
	}

	/** Get sentries firing interval */
	public static long getAlarmInterval() {
		return ALARM_INTERVAL;
	}

	/** Set sentries firing interval */
	private void setAlarmInterval(long interval) {
		ALARM_INTERVAL = interval;
	}

	public class ListGestureDetector extends SimpleOnGestureListener {

		private int FLING_MIN_DISTANCE;
		private int FLING_MAX_OFF_PATH;
		private double FLING_THRESHOLD_VELOCITY;

		public ListGestureDetector(int minDistance, int maxPath, double velocity) {
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

				int positionToRemove = mSentryItems.pointToPosition(
						(int) event1.getX(), (int) event1.getY());
				String repoName = ((Repository) mSentryAdapter
						.getItem(positionToRemove)).getRepositoryName();
				deleteItem(positionToRemove);
				mSentryAdapter.notifyDataSetChanged();

				Toast.makeText(getApplicationContext(),
						repoName + getResources().getText(R.string.toast_sentry_cancel),
						Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			int itemPosition = mSentryItems.pointToPosition((int) e.getX(),
					(int) e.getY());
			Repository repoItem = (Repository) mSentryAdapter
					.getItem(itemPosition);

			Intent commitHistoryIntent = new Intent(getApplicationContext(),
					CommitHistoryActivity.class);
			commitHistoryIntent.putExtra(SentryCreator.INTENT_KEY_REPO,
					repoItem);
			startActivity(commitHistoryIntent);

			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

	}
}
