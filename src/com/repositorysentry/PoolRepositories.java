package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

public class PoolRepositories {
	
	public static int ID;
	private static PoolRepositories mPool;
	private List<Repository> mRepositories = new ArrayList<Repository>();
	
	private PoolRepositories() { }

	public static PoolRepositories getInstance() {
		if (mPool == null) {
			mPool = new PoolRepositories();
		}
		return mPool;
	}
	
	public Repository getRepository(int id) {
		return mRepositories.get(id);
	}

	public void add(Repository item) {
		mRepositories.add(item);
	}
	
	public void remove(int id) {
		mRepositories.remove(id);
	}
	
	public void clearAll() {
		mRepositories.clear();
		ID = 0;
	}

	public int getSize() {
		return mRepositories.size();
	}

	public void loadPool(Context context, ArrayList<HashMap<String, String>> list) {
		for (HashMap<String, String> item : list) {
			int id = Integer.parseInt(item.get(Repository.ID_TAG));
			String username = item.get(Repository.NAME_TAG);
			String repositoryName = item.get(Repository.REPOSITORY_TAG);
			String vcsType = item.get(Repository.VCS_TAG);
			
			Repository repository = null;
			if(vcsType == "git") {
				repository = new GitRepository(context, id, username, repositoryName);
			} else if(vcsType == "bitbucket") {
				repository = new BitbucketRepository(context, id, username, repositoryName);
			}
			mRepositories.add(repository);
		}
	}
}
