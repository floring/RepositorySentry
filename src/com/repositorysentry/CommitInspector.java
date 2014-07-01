package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CommitInspector {
	
	private static CommitInspector mInspector;
	
	public DatabaseOpenHelper DbHelper;
	public SQLiteDatabase DB = null;

	private CommitInspector() { }

	public static CommitInspector getInstance() {
		if (mInspector == null) {
			mInspector = new CommitInspector();
		}
		return mInspector;
	}
	
	public ArrayList<HashMap<String, String>> getNewCommits(Repository repo,
			ArrayList<HashMap<String, String>> comitsHistory) {
		ArrayList<HashMap<String, String>> newComits = new ArrayList<HashMap<String, String>>();
		for (HashMap<String, String> item : comitsHistory) {
			String repoId = repo.getId().toString();
			String name = item.get(Repository.NAME_TAG);
			String date = item.get(Repository.DATE_TAG);
			String message = item.get(Repository.MESSAGE_TAG);
			if (!isRowExists(repoId, name, date, message)) {
				long requestCode = insertRow(repoId, repo.getRepositoryName(), name, date, message, repo.getType());

				HashMap<String, String> commitInfo = new HashMap<String, String>();
				commitInfo.put(Repository.NAME_TAG, name);
				commitInfo.put(Repository.DATE_TAG, date);
				commitInfo.put(Repository.MESSAGE_TAG, message);
				newComits.add(commitInfo);
			}
		}
		return newComits;
	}
	
	public ArrayList<HashMap<String, String>> getCommitsHistory(
			Repository repo) {
		String repoId = repo.getId().toString();
		ArrayList<HashMap<String, String>> commitsList = new ArrayList<HashMap<String, String>>();

		String[] columns = new String[] { DatabaseOpenHelper.NAME_COLUMN,
				DatabaseOpenHelper.DATE_COLUMN,
				DatabaseOpenHelper.MESSAGE_COLUMN };
		String whereClause = DatabaseOpenHelper.REPOSITORY_ID_COLUMN + "=?";
		String[] whereArgs = new String[] { repoId };

		Cursor cursor = DB.query(DatabaseOpenHelper.TABLE_NAME, columns,
				whereClause, whereArgs, null, null, null);
		
		int columnNameId = cursor.getColumnIndex(DatabaseOpenHelper.NAME_COLUMN);
		int columnDateId = cursor.getColumnIndex(DatabaseOpenHelper.DATE_COLUMN);
		int columnMsgId = cursor.getColumnIndex(DatabaseOpenHelper.MESSAGE_COLUMN);

		while (cursor.moveToNext()) {
			HashMap<String, String> commitInfo = new HashMap<String, String>();
			commitInfo.put(Repository.NAME_TAG, cursor.getString(columnNameId));
			commitInfo.put(Repository.DATE_TAG, cursor.getString(columnDateId));
			commitInfo.put(Repository.MESSAGE_TAG, cursor.getString(columnMsgId));
			commitsList.add(commitInfo);
		}
		cursor.close();
		return commitsList;
	}
	
	public void remove(Repository repo) {
		String repoId = repo.getId().toString();
		removeRow(repoId);
	}
	
	private void removeRow(String id) {
		String whereClause = DatabaseOpenHelper.REPOSITORY_ID_COLUMN + "=?";
		String[] whereArgs = new String[] { id };

		DB.delete(DatabaseOpenHelper.TABLE_NAME, whereClause, whereArgs);
	}
	
	private boolean isRowExists(String repoId,
			String name, String date, String message) {
		String[] columns = new String[] { DatabaseOpenHelper.REPOSITORY_ID_COLUMN,
				DatabaseOpenHelper.NAME_COLUMN, DatabaseOpenHelper.DATE_COLUMN,
				DatabaseOpenHelper.MESSAGE_COLUMN};
		String whereClause = DatabaseOpenHelper.REPOSITORY_ID_COLUMN + "=? AND "
				+ DatabaseOpenHelper.NAME_COLUMN + "=? AND "
				+ DatabaseOpenHelper.DATE_COLUMN + "=? AND "
				+ DatabaseOpenHelper.MESSAGE_COLUMN + "=?";
		String[] whereArgs = new String[] { repoId, name, date,
				message};
		Cursor cursor = DB.query(DatabaseOpenHelper.TABLE_NAME, columns,
				whereClause, whereArgs, null, null, null);
		int rowsCount = cursor.getCount();
		cursor.close();
		return (rowsCount > 0) ? true : false;
	}
	
	private long insertRow(String repoId, String repoName, String name, String date, String message, String vcs) {
		ContentValues values = new ContentValues();
		values.put(DatabaseOpenHelper.REPOSITORY_ID_COLUMN, repoId);
		values.put(DatabaseOpenHelper.REPOSITORY_COLUMN, repoName);
		values.put(DatabaseOpenHelper.NAME_COLUMN, name);
		values.put(DatabaseOpenHelper.DATE_COLUMN, date);
		values.put(DatabaseOpenHelper.MESSAGE_COLUMN, message);
		values.put(DatabaseOpenHelper.VCS_COLUMN, vcs);
		return DB.insert(DatabaseOpenHelper.TABLE_NAME, null, values);
	}

}
