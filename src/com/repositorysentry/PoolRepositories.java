package com.repositorysentry;

import java.util.ArrayList;
import java.util.List;

public class PoolRepositories {
	
	private static int ID = 0;
	private static PoolRepositories mPool;
	private List<Repository> mRepositories = new ArrayList<Repository>();
	
	private PoolRepositories() { }

	public static PoolRepositories getInstance() {
		if (mPool == null) {
			mPool = new PoolRepositories();
		}
		return mPool;
	}

	public void add(Repository item) {
		mRepositories.add(item);
		ID++;
	}

	public List<Repository> getPool() {
		return mRepositories;
	}
	
	public static int getLastId() {
		return ID;
	}
	
	public Repository getRepository(int id) {
		return mRepositories.get(id);
	}

}
