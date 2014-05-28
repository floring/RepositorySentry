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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.commit_history_listview);

		Intent intent = getIntent();
		String username = intent.getStringExtra("Username");
		String repositoryName = intent.getStringExtra("RepositoryName");

		CommitInspector inspector = CommitInspector.getInstance();
		if (inspector.mDB == null) {
			inspector.mDbHelper = new DatabaseOpenHelper(
					getApplicationContext());
			inspector.mDB = inspector.mDbHelper.getWritableDatabase();
		} else {
			if (!inspector.mDB.isOpen()) {
				inspector.mDbHelper = new DatabaseOpenHelper(
						getApplicationContext());
				inspector.mDB = inspector.mDbHelper.getWritableDatabase();
			}
		}
		ArrayList<HashMap<String, String>> commitsInfo = inspector
				.getCommitsHistoryFromDB(repositoryName);

		SimpleAdapter adapter = new SimpleAdapter(CommitHistoryActivity.this,
				(ArrayList<HashMap<String, String>>) commitsInfo.clone(),
				R.layout.commit_list_item, new String[] {
						CommitHistoryParsing.NAME_TAG,
						CommitHistoryParsing.DATE_TAG,
						CommitHistoryParsing.MESSAGE_TAG },
				new int[] { R.id.commiterName, R.id.commitDate,
						R.id.commitMessage });
		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);
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
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
