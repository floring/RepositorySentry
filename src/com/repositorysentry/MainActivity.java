package com.repositorysentry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
	
	private static final int CREATE_ALARM_ITEM_REQUEST = 0;
	private static final int MENU_DELETE = Menu.FIRST;
	private static final String FILE_NAME = "AlarmsActivityData.txt";
	
	private AlarmListAdapter mAlarmAdapter;
	private AlarmManager mAlarmManager;
	
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
		
		final Button buttonCreateAlarm = (Button) findViewById(R.id.button_alarm_list_create);
		buttonCreateAlarm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, CreateAlarmActivity.class);
				startActivityForResult(intent, CREATE_ALARM_ITEM_REQUEST);
			}
		});
		
		//setListAdapter(mAlarmAdapter);
		
		ListView alarmItems = (ListView) findViewById(android.R.id.list);
		alarmItems.setAdapter(mAlarmAdapter);
		alarmItems.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				
				final int positionToRemove = position;

				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		        dialog.setTitle("Delete?");
		        dialog.setMessage("Are you sure you want to delete this sentry?");
		        dialog.setNegativeButton("Cancel", null);
		        dialog.setPositiveButton("OK", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface d, int which) {
						// TODO Auto-generated method stub
						AlarmItem alarmItem = (AlarmItem) mAlarmAdapter.getItem(positionToRemove);
						PendingIntent pendingNoteIntent = composeRequiredIntent(alarmItem);
						mAlarmManager.cancel(pendingNoteIntent);
						
						mAlarmAdapter.removeItem(positionToRemove);
						
						mAlarmAdapter.notifyDataSetChanged();
					}		        
		        });
		        dialog.show();
		        
				return false;
			}			
		});
	}
	
	private PendingIntent composeRequiredIntent(AlarmItem alarmItem) {
		Bundle bundle = new Bundle();
		bundle.putString("Username", alarmItem.getUsername());
		bundle.putString("RepositoryName", alarmItem.getRepositoryName());
		
		Intent notifIntent = new Intent(getApplicationContext(), NotificationReceiver.class);
		notifIntent.putExtras(bundle);
		PendingIntent pendingNoteIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notifIntent, 0);
		
		return pendingNoteIntent;
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode == CREATE_ALARM_ITEM_REQUEST) {
			if(resultCode == RESULT_OK) {
				AlarmItem item = new AlarmItem(data);
				mAlarmAdapter.add(item);
			}
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		// Load saved AlarmItems, if necessary
		if (mAlarmAdapter.getCount() == 0)
			loadItems();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Save AlarmItems
		saveItems();
	}
	
	@Override
	protected void onDestroy() {
		//TODO: close database
		mInspector.mDB.close();
		mInspector.mDbHelper.deleteDatabase();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Reset all");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DELETE:
			mAlarmAdapter.clearAll();
			resetAllSentries();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void resetAllSentries() {		
		for(int i = 0; i < mAlarmAdapter.getCount(); i++) {
			AlarmItem alarmItem = (AlarmItem) mAlarmAdapter.getItem(i);
			PendingIntent pendingNoteIntent = composeRequiredIntent(alarmItem);
			
			mAlarmManager.cancel(pendingNoteIntent);
		}
		Toast.makeText(getApplicationContext(),
				"All sentries has been cancelled", Toast.LENGTH_LONG).show();
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

}
