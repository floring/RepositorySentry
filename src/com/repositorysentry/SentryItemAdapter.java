package com.repositorysentry;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SentryItemAdapter extends BaseAdapter implements Filterable {

	private List<Repository> mItems = new ArrayList<Repository>();
	private List<Repository> mAllItems = new ArrayList<Repository>();
	private final Context mContext;

	public SentryItemAdapter(Context context) {
		mContext = context;
	}

	/**
	 * Add a Repository to the adapter. Notify observers that the data set has
	 * changed
	 */
	public void add(Repository item) {
		mItems.add(item);
		notifyDataSetChanged();
	}

	public void clearAll() {
		mItems.clear();
		notifyDataSetChanged();
	}

	public void removeItem(int position) {
		mItems.remove(position);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	/**
	 * Get the ID for the RepositoryItem. In this case it's just the position
	 */
	@Override
	public long getItemId(int id) {
		return id;
	}
	
	public void setEmptyData(List<Repository> data) {
		mItems = data;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final Repository repoItem = (Repository) getItem(position);
		// Inflate the View for this RepoItem
		// from alarm_listview_item.xml.
		LayoutInflater sentryInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout itemLayout = (RelativeLayout) sentryInflater.inflate(
				R.layout.alarm_listview_item, null);

		final TextView usernameView = (TextView) itemLayout
				.findViewById(R.id.textView_alarm_listview_item_username);
		usernameView.setText(repoItem.getUsername());

		final TextView repoNameView = (TextView) itemLayout
				.findViewById(R.id.textView_alarm_listview_item_repository);
		repoNameView.setText(repoItem.getRepositoryName());

		final TextView dateView = (TextView) itemLayout
				.findViewById(R.id.textView_alarm_listview_item_date);
		dateView.setText(repoItem.getDate());
		
		int id = 0;
		if(repoItem.getType().equals(Vcs.Git.toString())) {
			id = R.drawable.ic_vcs_git;
		} else if(repoItem.getType().equals(Vcs.BitBucket.toString())) {
			id = R.drawable.ic_vcs_bitbucket;
		}
		final ImageView vcsView = (ImageView) itemLayout.findViewById(R.id.item_alarm_listview_vcs);
		vcsView.setImageResource(id);

		return itemLayout;
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();

				if (constraint == null || constraint.length() == 0) {
					filterResults.values = mAllItems;
					filterResults.count = mAllItems.size();
				} else {
					ArrayList<Repository> filteredRepoArray = new ArrayList<Repository>();

					constraint = constraint.toString().toLowerCase();
					for (int i = 0; i < mAllItems.size(); ++i) {
						String repoName = mAllItems.get(i).getRepositoryName();
						if (repoName.toLowerCase().startsWith(
								constraint.toString())) {
							filteredRepoArray.add(mAllItems.get(i));
						}
					}

					filterResults.count = filteredRepoArray.size();
					filterResults.values = filteredRepoArray;
				}

				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				mItems = (List<Repository>) results.values;
				notifyDataSetChanged();
			}

		};

		return filter;
	}

	/**
	 * Save all Alarm Items before filtering mAllItems that sort of buffer
	 */
	public void saveItems() {
		mAllItems = mItems;
	}
}
