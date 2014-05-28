package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CommitInspector {

	private static CommitInspector mInspector;

	public DatabaseOpenHelper mDbHelper;
	public SQLiteDatabase mDB = null;
	private ArrayList<HashMap<String, String>> mNewComitsData = new ArrayList<HashMap<String, String>>();

	private CommitInspector() {
	}

	public static CommitInspector getInstance() {
		if (mInspector == null) {
			mInspector = new CommitInspector();
		}
		return mInspector;
	}

	public ArrayList<HashMap<String, String>> getNewCommits(String repoName,
			ArrayList<HashMap<String, String>> comitsHistory) {
		mNewComitsData.clear();
		for (HashMap<String, String> item : comitsHistory) {
			String name = item.get(CommitHistoryParsing.NAME_TAG);
			String date = item.get(CommitHistoryParsing.DATE_TAG);
			String message = item.get(CommitHistoryParsing.MESSAGE_TAG);
			if (!isRowExists(repoName, name, date, message)) {
				long requestCode = insertRow(repoName, name, date, message);

				HashMap<String, String> commitInfo = new HashMap<String, String>();
				commitInfo.put(CommitHistoryParsing.REPOSITORY_TAG, repoName);
				commitInfo.put(CommitHistoryParsing.NAME_TAG, name);
				commitInfo.put(CommitHistoryParsing.DATE_TAG, date);
				commitInfo.put(CommitHistoryParsing.MESSAGE_TAG, message);
				mNewComitsData.add(commitInfo);
			}
		}
		return mNewComitsData;
	}

	public ArrayList<HashMap<String, String>> getCommitsHistoryFromDB(
			String repoName) {
		ArrayList<HashMap<String, String>> commitsList = new ArrayList<HashMap<String, String>>();

		String[] columns = new String[] { DatabaseOpenHelper.NAME_COLUMN,
				DatabaseOpenHelper.DATE_COLUMN,
				DatabaseOpenHelper.MESSAGE_COLUMN };
		String whereClause = DatabaseOpenHelper.REPOSITORY_COLUMN + "=?";
		String[] whereArgs = new String[] { repoName };

		Cursor cursor = mDB.query(DatabaseOpenHelper.TABLE_NAME, columns,
				whereClause, whereArgs, null, null, null);
		
		int columnNameId = cursor.getColumnIndex(DatabaseOpenHelper.NAME_COLUMN);
		int columnDateId = cursor.getColumnIndex(DatabaseOpenHelper.DATE_COLUMN);
		int columnMsgId = cursor.getColumnIndex(DatabaseOpenHelper.MESSAGE_COLUMN);

		while (cursor.moveToNext()) {
			HashMap<String, String> commitInfo = new HashMap<String, String>();
			commitInfo.put(CommitHistoryParsing.NAME_TAG, cursor.getString(columnNameId));
			commitInfo.put(CommitHistoryParsing.DATE_TAG, cursor.getString(columnDateId));
			commitInfo.put(CommitHistoryParsing.MESSAGE_TAG, cursor.getString(columnMsgId));
			commitsList.add(commitInfo);
		}
		cursor.close();
		return commitsList;
	}

	public void removeRepositoryRowsFromDB(String repoName) {
		String whereClause = DatabaseOpenHelper.REPOSITORY_COLUMN + "=?";
		String[] whereArgs = new String[] { repoName };

		mDB.delete(DatabaseOpenHelper.TABLE_NAME, whereClause, whereArgs);
	}

	private boolean isRowExists(String repositoryName, String name,
			String date, String message) {
		String[] columns = new String[] { DatabaseOpenHelper.REPOSITORY_COLUMN,
				DatabaseOpenHelper.NAME_COLUMN, DatabaseOpenHelper.DATE_COLUMN,
				DatabaseOpenHelper.MESSAGE_COLUMN };
		String whereClause = DatabaseOpenHelper.REPOSITORY_COLUMN + "=? AND "
				+ DatabaseOpenHelper.NAME_COLUMN + "=? AND "
				+ DatabaseOpenHelper.DATE_COLUMN + "=? AND "
				+ DatabaseOpenHelper.MESSAGE_COLUMN + "=?";
		String[] whereArgs = new String[] { repositoryName, name, date, message };
		Cursor cursor = mDB.query(DatabaseOpenHelper.TABLE_NAME, columns,
				whereClause, whereArgs, null, null, null);
		int rowsCount = cursor.getCount();
		cursor.close();
		return (rowsCount > 0) ? true : false;
	}

	private long insertRow(String repositoryName, String name, String date,
			String message) {
		ContentValues values = new ContentValues();
		values.put(DatabaseOpenHelper.REPOSITORY_COLUMN, repositoryName);
		values.put(DatabaseOpenHelper.NAME_COLUMN, name);
		values.put(DatabaseOpenHelper.DATE_COLUMN, date);
		values.put(DatabaseOpenHelper.MESSAGE_COLUMN, message);
		return mDB.insert(DatabaseOpenHelper.TABLE_NAME, null, values);
	}
}
