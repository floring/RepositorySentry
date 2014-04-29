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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AlarmListAdapter extends BaseAdapter implements Filterable {
	
	private List<AlarmItem> mItems = new ArrayList<AlarmItem>();
	private List<AlarmItem> mAllItems = new ArrayList<AlarmItem>();
	
	private final Context mContext;
	
	public AlarmListAdapter(Context context) {
		mContext = context;
	}
	
	// Add a AlarmItem to the adapter
	// Notify observers that the data set has changed
	public void add(AlarmItem item) {
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
		// TODO Auto-generated method stub
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mItems.get(position);
	}
	
	
	// Get the ID for the ToDoItem
	// In this case it's just the position
	
	@Override
	public long getItemId(int id) {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		final AlarmItem alarmItem = (AlarmItem) getItem(position);	

		// Inflate the View for this AlarmItem
		// from alarm_listview_item.xml.
		LayoutInflater alarmInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout itemLayout = (RelativeLayout)alarmInflater.inflate(R.layout.alarm_listview_item, null);

		final TextView usernameView = (TextView) itemLayout.findViewById(R.id.textView_alarm_listview_item_username);
		usernameView.setText(alarmItem.getUsername());
		
		final TextView repoNameView = (TextView) itemLayout.findViewById(R.id.textView_alarm_listview_item_repository);
		repoNameView.setText(alarmItem.getRepositoryName());
		
		final TextView dateView = (TextView) itemLayout.findViewById(R.id.textView_alarm_listview_item_date);
		dateView.setText(AlarmItem.FORMAT.format(alarmItem.getDate()));

		return itemLayout;
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();

				if(constraint == null || constraint.length() == 0) {
					filterResults.values = mAllItems;
					filterResults.count = mAllItems.size();
				}
				else {
					ArrayList<AlarmItem> filteredRepoArray = new ArrayList<AlarmItem>();
					
					constraint = constraint.toString().toLowerCase();
					for(int i = 0; i < mAllItems.size(); ++i) {
						String repoName = mAllItems.get(i).getRepositoryName();
						if(repoName.toLowerCase().startsWith(constraint.toString())) {
							filteredRepoArray.add(mAllItems.get(i));
						}
					}
					
					filterResults.count = filteredRepoArray.size();
					filterResults.values = filteredRepoArray;
				}
				
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				mItems = (List<AlarmItem>) results.values;
				notifyDataSetChanged();
			}
			
		};
		
		return filter;
	}
	
	
	// Save all Alarm Items before filtering
	// mAllItems that sort of buffer
	public void saveItems() {
		mAllItems = mItems;
	}
	

}
