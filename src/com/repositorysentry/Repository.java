package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public abstract class Repository {
	
	public static final String ID_TAG = "id";
	public static final String COMMIT_TAG = "commit";
	public static final String MESSAGE_TAG = "message";
	public static final String COMMITER_TAG = "committer";
	public static final String NAME_TAG = "name";
	public static final String DATE_TAG = "date";
	public static final String REPOSITORY_TAG = "repository";
	public static final String VCS_TAG = "vcs";
	public static final String LETTERS = "[A-Za-z]";
	
	protected abstract void setAlarm();
	protected abstract String getUrl();
	protected abstract String getRepositoryName();
	protected abstract ArrayList<HashMap<String, String>> parseJSON(String jsonStr);
	protected abstract String getType();
	protected abstract int getRepoId();
	
	public ArrayList<HashMap<String, String>> getCommitsHistory() {
		String url = getUrl();
		HttpGetTask task = new HttpGetTask();
		task.execute(url);
		try {
			return parseJSON(task.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return new ArrayList<HashMap<String, String>>();
	}

}
