package com.repositorysentry;

public class GitRepository extends Repository {
	
	private String mUsername;
	private String mRepositoryName;
	
	public GitRepository(String username, String repositoryName) {
		mUsername = username;
		mRepositoryName = repositoryName;
	}
	
	
}
