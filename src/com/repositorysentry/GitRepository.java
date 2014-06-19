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

public class GitRepository extends Repository {
	
	private static final String TYPE = Vcs.Git.toString();

	public GitRepository(Context context, String username, String repositoryName) {
		mId = UUID.randomUUID();
		mContext = context;
		mUsername = username;
		mRepositoryName = repositoryName;
		mDate = getDateString();
	}

	@Override
	protected String getUrl() {
		String url = String.format(
				"https://api.github.com/repos/%s/%s/commits", mUsername,
				mRepositoryName);
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
	public static final Parcelable.Creator<GitRepository> CREATOR = new Parcelable.Creator<GitRepository>() {
		public GitRepository createFromParcel(Parcel in) {
			return new GitRepository(in);
		}

		public GitRepository[] newArray(int size) {
			return new GitRepository[size];
		}
	};

	/** Constructor which reads data from Parcel. */
	private GitRepository(Parcel in) {
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
				JSONArray jsonArray = new JSONArray(jsonStr);
				JSONObject jsonObject;

				for (int i = 0; i < jsonArray.length(); ++i) {
					jsonObject = jsonArray.getJSONObject(i);

					jsonObject = jsonObject.getJSONObject(COMMIT_TAG);
					String message = jsonObject.getString(MESSAGE_TAG);

					jsonObject = jsonObject.getJSONObject(COMMITER_TAG);
					String name = jsonObject.getString(NAME_TAG);
					String date = jsonObject.getString(DATE_TAG);
					date = date.replaceAll(LETTERS, " ");			

					HashMap<String, String> commitInfo = new HashMap<String, String>();
					commitInfo.put(NAME_TAG, name);
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
