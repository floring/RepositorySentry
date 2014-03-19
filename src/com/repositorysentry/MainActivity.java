package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {

	private EditText mUsername, mRepositoryName;
	private Button mButtonGetCommit;
	private CommitHistory mCommit;
	
	// Notification ID to allow for future updates
	private static final int MY_NOTIFICATION_ID = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
		mCommit = new CommitHistory(this);
		mUsername = (EditText) findViewById(R.id.username);
		mRepositoryName = (EditText) findViewById(R.id.repository);
		mButtonGetCommit = (Button) findViewById(R.id.get_commit_button);
		mButtonGetCommit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String username = mUsername.getText().toString();
				String repoName = mRepositoryName.getText().toString();
				if(!username.isEmpty() && !repoName.isEmpty()) {
					ArrayList<HashMap<String, String>> commitsInfo = mCommit.getCommitsHistory(username, repoName);
					
					displayCommitsList(commitsInfo);
					
					ArrayList<HashMap<String, String>> newCommitsData = mCommit.getNewCommitsData(repoName);
					if(!newCommitsData.isEmpty()) {
						createCommitNotification(newCommitsData);
					}
				}
			}
		});
	}
	
	private void displayCommitsList(ArrayList<HashMap<String, String>> commitsInfo) {
		SimpleAdapter adapter = new SimpleAdapter(
				MainActivity.this, 
				(ArrayList<HashMap<String,String>>) commitsInfo.clone(), 
				R.layout.list_item, 
				new String[] { CommitHistory.NAME_TAG, CommitHistory.DATE_TAG, CommitHistory.MESSAGE_TAG }, 
				new int[] { R.id.commiterName, R.id.commitDate, R.id.commitMessage });
		
		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);
	}
	
	private void createCommitNotification(ArrayList<HashMap<String, String>> newCommitsData) {
		CharSequence tickerText = "You get " + newCommitsData.size() + " new commits";
		CharSequence contentTitle = "New Commits";
		CharSequence contentText = "You've Been Notified!";
		
		Notification.Builder notificationBuilder = new Notification.Builder(
				getApplicationContext())
				.setTicker(tickerText)
				.setSmallIcon(android.R.drawable.stat_sys_warning)
				.setAutoCancel(true)
				.setContentTitle(contentTitle)
				.setContentText(contentText);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());
	}
	
	@Override
	protected void onPause() {

		super.onPause();
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

	/*
	  A placeholder fragment containing a simple view.
	 
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}*/

}
