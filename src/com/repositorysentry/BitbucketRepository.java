package com.repositorysentry;

import java.util.UUID;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class BitbucketRepository extends Repository {
	
	private static final String TYPE = Vcs.BitBucket.toString();

	public BitbucketRepository(Context context, String username,
			String repositoryName) {
		mId = UUID.randomUUID();
		mContext = context;
		mUsername = username;
		mRepositoryName = repositoryName;
		mDate = getDateString();
	}

	@Override
	protected String getUrl() {
		String url = String.format(
				"https://bitbucket.org/api/2.0/repositories/%s/%s/commits/",
				mUsername, mRepositoryName);
		return url;
	}

	/**
	 * Returns kinds of special objects contained in this Parcelable's
	 * representation. In this case returns 0.
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/** Packs object from Parcel. */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mUsername);
		dest.writeString(mRepositoryName);
		dest.writeString(mDate);
		dest.writeValue(mId);
	}

	/** Unpacks object from Parcel. */
	public static final Parcelable.Creator<BitbucketRepository> CREATOR = new Parcelable.Creator<BitbucketRepository>() {
		public BitbucketRepository createFromParcel(Parcel in) {
			return new BitbucketRepository(in);
		}

		public BitbucketRepository[] newArray(int size) {
			return new BitbucketRepository[size];
		}
	};

	/** Constructor which reads data from Parcel. */
	private BitbucketRepository(Parcel in) {
		mUsername = in.readString();
		mRepositoryName = in.readString();
		mId = (UUID) in.readValue(null);
		mDate = in.readString();
	}

	@Override
	protected String getType() {
		return TYPE;
	}

}
