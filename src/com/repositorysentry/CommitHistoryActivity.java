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

	private int mRepositoryId;
	private CommitInspector mInspector = CommitInspector.getInstance();
	private ArrayList<HashMap<String, String>> mCommitList = new ArrayList<HashMap<String, String>>();
	private SimpleAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.commit_history_listview);

		Intent intent = getIntent();
		mRepositoryId = intent.getIntExtra("Id", 0);

		mCommitList = mInspector.getCommitsHistoryFromDB(String
				.valueOf(mRepositoryId));

		mAdapter = new SimpleAdapter(CommitHistoryActivity.this, mCommitList,
				R.layout.commit_list_item, new String[] { Repository.NAME_TAG,
						Repository.DATE_TAG, Repository.MESSAGE_TAG },
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
		PoolRepositories pool = PoolRepositories.getInstance();
		Repository repository = pool.getRepository(mRepositoryId);
		ArrayList<HashMap<String, String>> commitsData = repository
				.getCommitsHistory();

		refreshAdapter(commitsData);
	}

	private void refreshAdapter(ArrayList<HashMap<String, String>> commits) {
		mCommitList = commits;
		mAdapter.notifyDataSetChanged();
	}
}
