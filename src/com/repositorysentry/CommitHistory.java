package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
public class CommitHistory {

	public static final String COMMIT_TAG = "commit";
	public static final String MESSAGE_TAG = "message";
	public static final String COMMITER_TAG = "committer";
	public static final String NAME_TAG = "name";
	public static final String DATE_TAG = "date";
	public static final String REPOSITORY_TAG = "repository";

	private Context mContext;
	private ArrayList<HashMap<String, String>> mComitsData = new ArrayList<HashMap<String, String>>();	

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
}
