package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class CommitHistoryActivity extends ListActivity {
	
	private String mUsername;
	private String mRepositoryName;
	private CommitInspector mInspector = CommitInspector.getInstance();
	private ArrayList<HashMap<String, String>> mCommitList = new ArrayList<HashMap<String,String>>();
	private SimpleAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.commit_history_listview);

		Intent intent = getIntent();
		mUsername = intent.getStringExtra("Username");
		mRepositoryName = intent.getStringExtra("RepositoryName");

		mCommitList = mInspector
				.getCommitsHistoryFromDB(mRepositoryName);
	
		mAdapter = new SimpleAdapter(CommitHistoryActivity.this,
				mCommitList,
				R.layout.commit_list_item, new String[] {
						CommitHistoryParsing.NAME_TAG,
						CommitHistoryParsing.DATE_TAG,
						CommitHistoryParsing.MESSAGE_TAG },
				new int[] { R.id.commiterName, R.id.commitDate, 
						R.id.commitMessage });
		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.refresh_history, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			refreshCommitHistory();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void refreshCommitHistory() {
		CommitHistoryParsing commitHistory = new CommitHistoryParsing(this);
		ArrayList<HashMap<String, String>> commitsInfo = commitHistory
				.getCommitsHistory(mUsername, mRepositoryName);
		
		refreshAdapter(commitsInfo);
	}
	
	private void refreshAdapter(ArrayList<HashMap<String, String>> commits) {		
		mCommitList = commits;
		mAdapter.notifyDataSetChanged();
	}
}
