package com.repositorysentry;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommitAdapter extends BaseAdapter {

	private ArrayList<HashMap<String, String>> mCommits;
	private Context mContext;

	public CommitAdapter(Context c, ArrayList<HashMap<String, String>> commits) {
		mContext = c;
		mCommits = commits;
	}

	public void refresh(ArrayList<HashMap<String, String>> commits) {
		mCommits.clear();
		mCommits.addAll(commits);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mCommits.size();
	}

	@Override
	public Object getItem(int position) {
		return mCommits.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater commitInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = commitInflater.inflate(R.layout.commit_list_item,
					parent, false);
		}
		final HashMap<String, String> commitItem = (HashMap<String, String>) getItem(position);

		final TextView commiterNameView = (TextView) convertView
				.findViewById(R.id.commiterName);
		commiterNameView.setText(commitItem.get(Repository.NAME_TAG));

		final TextView commitDateView = (TextView) convertView
				.findViewById(R.id.commitDate);
		commitDateView.setText(commitItem.get(Repository.DATE_TAG));

		final TextView commitMessageView = (TextView) convertView
				.findViewById(R.id.commitMessage);
		commitMessageView.setText(commitItem.get(Repository.MESSAGE_TAG));

		return convertView;
	}
}