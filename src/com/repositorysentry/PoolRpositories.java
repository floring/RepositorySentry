package com.repositorysentry;

import java.util.ArrayList;
import java.util.List;

public class PoolRpositories {

	private List<Repository> mRepositories = new ArrayList<Repository>();

	public void add(Repository item) {
		mRepositories.add(item);
	}

	public List<Repository> getPool() {
		return mRepositories;
	}

}
