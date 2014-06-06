package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CommitInspector {

	private static CommitInspector mInspector;

	public DatabaseOpenHelper mDbHelper;
	public SQLiteDatabase mDB = null;
	private ArrayList<HashMap<String, String>> mNewComitsData = new ArrayList<HashMap<String, String>>();

	private CommitInspector() { }

	public static CommitInspector getInstance() {
		if (mInspector == null) {
			mInspector = new CommitInspector();
		}
		return mInspector;
	}

	public ArrayList<HashMap<String, String>> getNewCommits(String repoId, String vcs, String repoName,
			ArrayList<HashMap<String, String>> comitsHistory) {
		mNewComitsData.clear();
		for (HashMap<String, String> item : comitsHistory) {
			String name = item.get(Repository.NAME_TAG);
			String date = item.get(Repository.DATE_TAG);
			String message = item.get(Repository.MESSAGE_TAG);
			if (!isRowExists(repoId, repoName, name, date, message, vcs)) {
				long requestCode = insertRow(repoId, repoName, name, date, message, vcs);

				HashMap<String, String> commitInfo = new HashMap<String, String>();
				commitInfo.put(Repository.REPOSITORY_TAG, repoName);
				commitInfo.put(Repository.NAME_TAG, name);
				commitInfo.put(Repository.DATE_TAG, date);
				commitInfo.put(Repository.MESSAGE_TAG, message);
				mNewComitsData.add(commitInfo);
			}
		}
		return mNewComitsData;
	}

	public ArrayList<HashMap<String, String>> getCommitsHistoryFromDB(
			String repoId) {
		ArrayList<HashMap<String, String>> commitsList = new ArrayList<HashMap<String, String>>();

		String[] columns = new String[] { DatabaseOpenHelper.NAME_COLUMN,
				DatabaseOpenHelper.DATE_COLUMN,
				DatabaseOpenHelper.MESSAGE_COLUMN };
		String whereClause = DatabaseOpenHelper.REPOSITORY_ID_COLUMN + "=?";
		String[] whereArgs = new String[] { repoId };

		Cursor cursor = mDB.query(DatabaseOpenHelper.TABLE_NAME, columns,
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
	
	public ArrayList<HashMap<String, String>> getRepositoriesFromDB() {
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		
		String[] columns = new String[] { DatabaseOpenHelper.REPOSITORY_ID_COLUMN,
				DatabaseOpenHelper.REPOSITORY_COLUMN,
				DatabaseOpenHelper.NAME_COLUMN,
				DatabaseOpenHelper.VCS_COLUMN };
		
		Cursor cursor = mDB.query(DatabaseOpenHelper.TABLE_NAME, columns,
				null, null, null, null, null);
		
		int columnRepoId = cursor.getColumnIndex(DatabaseOpenHelper.REPOSITORY_ID_COLUMN);
		int columnRepositoryId = cursor.getColumnIndex(DatabaseOpenHelper.REPOSITORY_COLUMN);
		int columnNameId = cursor.getColumnIndex(DatabaseOpenHelper.NAME_COLUMN);
		int columnVcsId = cursor.getColumnIndex(DatabaseOpenHelper.VCS_COLUMN);

		while (cursor.moveToNext()) {
			HashMap<String, String> repoInfo = new HashMap<String, String>();
			repoInfo.put(Repository.ID_TAG, cursor.getString(columnRepoId));
			repoInfo.put(Repository.REPOSITORY_TAG, cursor.getString(columnRepositoryId));
			repoInfo.put(Repository.NAME_TAG, cursor.getString(columnNameId));
			repoInfo.put(Repository.VCS_TAG, cursor.getString(columnVcsId));
			list.add(repoInfo);
		}
		cursor.close();		
		return list;
	}

	public void removeRepositoryRowsFromDB(String repoId) {
		String whereClause = DatabaseOpenHelper.REPOSITORY_ID_COLUMN + "=?";
		String[] whereArgs = new String[] { repoId };

		mDB.delete(DatabaseOpenHelper.TABLE_NAME, whereClause, whereArgs);
	}

	private boolean isRowExists(String repoId, String repositoryName,
			String name, String date, String message, String vcs) {
		String[] columns = new String[] { DatabaseOpenHelper.REPOSITORY_COLUMN,
				DatabaseOpenHelper.NAME_COLUMN, DatabaseOpenHelper.DATE_COLUMN,
				DatabaseOpenHelper.MESSAGE_COLUMN,
				DatabaseOpenHelper.REPOSITORY_ID_COLUMN,
				DatabaseOpenHelper.VCS_COLUMN };
		String whereClause = DatabaseOpenHelper.REPOSITORY_COLUMN + "=? AND "
				+ DatabaseOpenHelper.NAME_COLUMN + "=? AND "
				+ DatabaseOpenHelper.DATE_COLUMN + "=? AND "
				+ DatabaseOpenHelper.MESSAGE_COLUMN + "=? AND "
				+ DatabaseOpenHelper.REPOSITORY_ID_COLUMN + "=? AND "
				+ DatabaseOpenHelper.VCS_COLUMN + "=?";
		String[] whereArgs = new String[] { repositoryName, name, date,
				message, repoId, vcs };
		Cursor cursor = mDB.query(DatabaseOpenHelper.TABLE_NAME, columns,
				whereClause, whereArgs, null, null, null);
		int rowsCount = cursor.getCount();
		cursor.close();
		return (rowsCount > 0) ? true : false;
	}

	private long insertRow(String repoId, String repositoryName, String name, String date,
			String message, String vcs) {
		ContentValues values = new ContentValues();
		values.put(DatabaseOpenHelper.REPOSITORY_COLUMN, repositoryName);
		values.put(DatabaseOpenHelper.NAME_COLUMN, name);
		values.put(DatabaseOpenHelper.DATE_COLUMN, date);
		values.put(DatabaseOpenHelper.MESSAGE_COLUMN, message);
		values.put(DatabaseOpenHelper.REPOSITORY_ID_COLUMN, repoId);
		values.put(DatabaseOpenHelper.VCS_COLUMN, vcs);
		return mDB.insert(DatabaseOpenHelper.TABLE_NAME, null, values);
	}
}
