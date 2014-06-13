package com.repositorysentry;

import java.util.UUID;

import android.content.Context;
import android.os.Parcelable;

public abstract class Repository implements Parcelable {
	
	protected UUID mId;
	protected String mUsername;
	protected String mRepositoryName;
	protected Context mContext;
	
	protected UUID getId() {
		return mId;
	}
	
	protected String getUsername() {
		return mUsername;
	}
	
	protected String getRepositoryName() {
		return mRepositoryName;
	}
	
	protected abstract String getUrl();


}
