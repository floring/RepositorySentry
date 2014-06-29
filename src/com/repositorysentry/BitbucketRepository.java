package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class BitbucketRepository extends Repository {
	
	private static final String TYPE = Vcs.BitBucket.toString();
	
	public static final String AUTHOR_TAG = "author";
	public static final String VALUES_TAG = "values";
	public static final String RAW_TAG = "raw";

	public BitbucketRepository(Context context, String username,
			String repositoryName) {
		mId = UUID.randomUUID();
		mContext = context;
		mUsername = username;
		mRepositoryName = repositoryName;
		mDate = getDateString();
	}
	
	public BitbucketRepository(UUID id, Context context, String username, String repositoryName, String date) {
		mId = id;
		mContext = context;
		mUsername = username;
		mRepositoryName = repositoryName;
		mDate = date;
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
		mDate = in.readString();
		mId = (UUID) in.readValue(null);		
	}

	@Override
	protected String getType() {
		return TYPE;
	}

	@Override
	protected ArrayList<HashMap<String, String>> parseJSON(String jsonStr) {
		ArrayList<HashMap<String, String>> comitsData = new ArrayList<HashMap<String, String>>();
		if (jsonStr != null && !jsonStr.isEmpty()) {
			try {
				JSONObject jsonObject = new JSONObject(jsonStr);
				JSONArray jsonArray = jsonObject.getJSONArray(VALUES_TAG);
				for(int i = 0; i < jsonArray.length(); ++i) {
					jsonObject = jsonArray.getJSONObject(i);
					
					String date = jsonObject.getString(DATE_TAG).split("\\+")[0];
					date = date.replaceAll(LETTERS, " ");
					String message = jsonObject.getString(MESSAGE_TAG);
					
					jsonObject = jsonObject.getJSONObject(AUTHOR_TAG);
					String author = jsonObject.getString(RAW_TAG).split(" ")[0];
					
					HashMap<String, String> commitInfo = new HashMap<String, String>();
					commitInfo.put(NAME_TAG, author);
					commitInfo.put(DATE_TAG, date);
					commitInfo.put(MESSAGE_TAG, message);

					comitsData.add(commitInfo);					
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return comitsData;
	}
}
