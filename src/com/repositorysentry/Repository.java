package com.repositorysentry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.os.Parcelable;

public abstract class Repository implements Parcelable {
	
	protected UUID mId;
	protected String mUsername;
	protected String mRepositoryName;
	protected String mDate;
	protected Context mContext;	
	
	public static final String COMMIT_TAG = "commit";
	public static final String MESSAGE_TAG = "message";
	public static final String COMMITER_TAG = "committer";
	public static final String NAME_TAG = "name";
	public static final String DATE_TAG = "date";
	public static final String LETTERS = "[A-Za-z]";
	
	protected UUID getId() {
		return mId;
	}
	
	protected String getUsername() {
		return mUsername;
	}
	
	protected String getRepositoryName() {
		return mRepositoryName;
	}
	
	protected String getDate() {
		return mDate;
	}
	
	protected abstract String getUrl();
	
	protected abstract String getType();
	
	protected abstract ArrayList<HashMap<String, String>> parseJSON(String jsonStr);
	
	public ArrayList<HashMap<String, String>> getCommitsHistory() {
		String url = getUrl();
		HttpGetTask task = new HttpGetTask();
		task.execute(url);
		try {
			String jsonStr = task.get();
			return (jsonStr.isEmpty()) ? null : parseJSON(jsonStr);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected String getDateString() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int monthOfYear = c.get(Calendar.MONTH);
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

		// Increment monthOfYear for Calendar/Date -> Time Format setting
		monthOfYear++;
		String mon = "" + monthOfYear;
		String day = "" + dayOfMonth;

		if (monthOfYear < 10)
			mon = "0" + monthOfYear;
		if (dayOfMonth < 10)
			day = "0" + dayOfMonth;

		String dateString = day + "." + mon + "." + year;

		return dateString;
	}

}
