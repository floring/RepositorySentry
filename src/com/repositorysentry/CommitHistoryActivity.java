package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class CommitHistoryActivity extends ListActivity {

	private Repository mRepository;
	private CommitInspector mInspector = CommitInspector.getInstance();

	private CommitAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.commit_history_listview);

		mRepository = getIntent().getParcelableExtra(
				SentryCreator.INTENT_KEY_REPO);
		ArrayList<HashMap<String, String>> commitList = mInspector
				.getCommitsHistory(mRepository);

		mAdapter = new CommitAdapter(this, commitList);
		setListAdapter(mAdapter);
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
		ArrayList<HashMap<String, String>> commits = mRepository.getCommits();
		mInspector.getNewCommits(mRepository, commits);
		mAdapter.refresh(commits);
	}
}
