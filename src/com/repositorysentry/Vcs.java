package com.repositorysentry;


public enum Vcs {
	Git(R.string.vcs_git), BitBucket(R.string.vcs_bitbucket);

	private int resourceId;

	private Vcs(int id) {
		resourceId = id;		
	}
}
