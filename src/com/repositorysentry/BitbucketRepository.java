package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

public class BitbucketRepository extends Repository {

	private int mId;
	private Context mContext;
	private String mUsername;
	private String mRepositoryName;
	
	public static final String AUTHOR_TAG = "author";
	public static final String VALUES_TAG = "values";
	public static final String RAW_TAG = "raw";

	public BitbucketRepository(Context context, int id, String username,
			String repositoryName) {
		mContext = context;
		mId = id;
		mUsername = username;
		mRepositoryName = repositoryName;
	}

	@Override
	public void setAlarm() {
		AlarmManager alarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		Bundle bundle = new Bundle();
		bundle.putInt("Id", mId);
		Intent notificationIntent = new Intent(mContext,
				NotificationReceiver.class);
		notificationIntent.putExtras(bundle);

		PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0,
				notificationIntent, 0);

		// TODO: change 3nd parameter to CreateAlarmActivity.ALARM_DELAY
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime(),
				CreateAlarmActivity.INITIAL_ALARM_DELAY, contentIntent);

		Toast.makeText(mContext, "Repository Sentry Set", Toast.LENGTH_LONG)
				.show();

	}

	@Override
	protected String getUrl() {
		String url = String.format(
				"https://bitbucket.org/api/2.0/repositories/%s/%s/commits/",
				mUsername, mRepositoryName);
		return url;
	}

	@Override
	protected ArrayList<HashMap<String, String>> parseJSON(String jsonStr) {
		ArrayList<HashMap<String, String>> comitsData = new ArrayList<HashMap<String, String>>();
		if (jsonStr != null && !jsonStr.isEmpty()) {
			try {
				JSONObject jsonObject = new JSONObject(jsonStr);
				JSONArray jsonArray = jsonObject.getJSONArray(VALUES_TAG);
				for(int i = 0; i < jsonArray.length(); ++i) {
					jsonObject = jsonArray.getJSONObject(i);
					
					String date = jsonObject.getString(DATE_TAG).split("+")[0];
					date = date.replaceAll(LETTERS, " ");
					String message = jsonObject.getString(MESSAGE_TAG);
					
					jsonObject = jsonObject.getJSONObject(AUTHOR_TAG);
					String author = jsonObject.getString(RAW_TAG).split(" ")[0];
					
					HashMap<String, String> commitInfo = new HashMap<String, String>();
					commitInfo.put(NAME_TAG, author);
					commitInfo.put(DATE_TAG, date);
					commitInfo.put(MESSAGE_TAG, message);

					comitsData.add(commitInfo);					
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return comitsData;
	}
	
	@Override
	protected String getRepositoryName() {
		return mRepositoryName;
	}

	@Override
	protected String getType() {
		return "bitbucket";
	}
}
