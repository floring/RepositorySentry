package com.repositorysentry;

public class BitbucketRepository extends Repository {

	private String mUsername;
	private String mRepositoryName;
	
	public BitbucketRepository(String username, String repositoryName) {
		mUsername = username;
		mRepositoryName = repositoryName;
	}
}
