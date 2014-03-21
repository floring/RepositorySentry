package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CommitHistory {

	public static final String COMMIT_TAG = "commit";
	public static final String MESSAGE_TAG = "message";
	public static final String COMMITER_TAG = "committer";
	public static final String NAME_TAG = "name";
	public static final String DATE_TAG = "date";
	public static final String REPOSITORY_TAG = "repository";

	private Context mContext;
	public DatabaseOpenHelper mDbHelper;
	public SQLiteDatabase mDB = null;
	private ArrayList<HashMap<String, String>> mComitsData = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String, String>> mNewComitsData = new ArrayList<HashMap<String, String>>();

	public CommitHistory(Context context) {
		mContext = context;
	}

	public ArrayList<HashMap<String, String>> getCommitsHistory(
			String username, String repoName) {
		// TODO: possible security hole
		String url = String.format(
				"https://api.github.com/repos/%s/%s/commits", username,
				repoName);

		HttpGetTask task = new HttpGetTask();
		task.execute(url);
		try {
			return parseJSON(task.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return mComitsData;
	}

	private ArrayList<HashMap<String, String>> parseJSON(String jsonStr) {
		mComitsData.clear();
		if (jsonStr != null && !jsonStr.isEmpty()) {
			try {
				JSONArray jsonArray = new JSONArray(jsonStr);
				JSONObject jsonObject;

				for (int i = 0; i < jsonArray.length(); ++i) {
					jsonObject = jsonArray.getJSONObject(i);

					jsonObject = jsonObject.getJSONObject(COMMIT_TAG);
					String message = jsonObject.getString(MESSAGE_TAG);

					jsonObject = jsonObject.getJSONObject(COMMITER_TAG);
					String name = jsonObject.getString(NAME_TAG);
					String date = jsonObject.getString(DATE_TAG);

					HashMap<String, String> commitInfo = new HashMap<String, String>();
					commitInfo.put(NAME_TAG, name);
					commitInfo.put(DATE_TAG, date);
					commitInfo.put(MESSAGE_TAG, message);

					mComitsData.add(commitInfo);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return mComitsData;
	}

	public ArrayList<HashMap<String, String>> getNewCommitsData(String repoName) {
		mNewComitsData.clear();
		for (HashMap<String, String> item : mComitsData) {
			String name = item.get(NAME_TAG);
			String date = item.get(DATE_TAG);
			String message = item.get(MESSAGE_TAG);
			if (!ifRowExists(repoName, name, date, message)) {
				long requestCode = insertRow(repoName, name, date, message);

				HashMap<String, String> commitInfo = new HashMap<String, String>();
				commitInfo.put(REPOSITORY_TAG, repoName);
				commitInfo.put(NAME_TAG, name);
				commitInfo.put(DATE_TAG, date);
				commitInfo.put(MESSAGE_TAG, message);
				mNewComitsData.add(commitInfo);
			}
		}
		return mNewComitsData;
	}

	private boolean ifRowExists(String repositoryName, String name, String date, String message) {
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
