package com.repositorysentry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.content.Intent;

public class AlarmItem {

	public static final String ITEM_SEP = System.getProperty("line.separator");

	public final static String USERNAME = "username";
	public final static String REPOSITORY_NAME = "repositoryName";
	public final static String DATE = "date";
	public final static String REPOSITORY_ID = "repositoryId";

	public final static SimpleDateFormat FORMAT = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss", Locale.getAvailableLocales()[0]);

	private String mUsername = new String();
	private String mRepository = new String();
	private Date mDate = new Date();
	private int mRepoId;

	AlarmItem(String username, String repoName, Date date, int repoId) {
		mUsername = username;
		mRepository = repoName;
		mDate = date;
		mRepoId = repoId;
	}

	// Create a new AlarmItem from data packaged in an Intent
	AlarmItem(Intent intent) {

		mUsername = intent.getStringExtra(AlarmItem.USERNAME);
		mRepository = intent.getStringExtra(AlarmItem.REPOSITORY_NAME);
		mRepoId = intent.getIntExtra(AlarmItem.REPOSITORY_ID, 0);

		try {
			mDate = AlarmItem.FORMAT.parse(intent
					.getStringExtra(AlarmItem.DATE));
		} catch (ParseException e) {
			mDate = new Date();
		}
	}

	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String username) {
		mUsername = username;
	}

	public String getRepositoryName() {
		return mRepository;
	}

	public void setRepositoryName(String repositoryName) {
		mRepository = repositoryName;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}
	
	public int getRepositoryId() {
		return mRepoId;
	}
	
	// Takes a set of String data values and
	// packages them for transport in an Intent

	public static void packageIntent(Intent intent, String username,
			String repoName, String date, int repoId) {

		intent.putExtra(AlarmItem.USERNAME, username);
		intent.putExtra(AlarmItem.REPOSITORY_NAME, repoName);
		intent.putExtra(AlarmItem.DATE, date);
		intent.putExtra(AlarmItem.REPOSITORY_ID, repoId);
	}

	public String toString() {
		return mUsername + ITEM_SEP + mRepository + ITEM_SEP
				+ FORMAT.format(mDate);
	}

}
