package com.repositorysentry;

import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.Toast;

public class ListGestureDetector extends SimpleOnGestureListener {
	
	private int FLING_MIN_DISTANCE;
	private int FLING_MAX_OFF_PATH;
	private double FLING_THRESHOLD_VELOCITY;
	
	private SentryItemAdapter mSentryAdapter;
	private ListView mSentryItems;
	private Context mContext;

	public ListGestureDetector(Context c, SentryItemAdapter adapter, ListView items, int minDistance, int maxPath,
			double velocity) {
		mContext = c;
		mSentryAdapter = adapter;
		mSentryItems = items;
		FLING_MIN_DISTANCE = minDistance;
		FLING_MAX_OFF_PATH = maxPath;
		FLING_THRESHOLD_VELOCITY = velocity;
	}
	
	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2,
			float velocityX, float velocityY) {
		if (Math.abs(event1.getY() - event2.getY()) > FLING_MAX_OFF_PATH) {
			return false;
		}
		// if: Right to Left fling
		// else: Left to Right fling
		if (event1.getX() - event2.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_THRESHOLD_VELOCITY) {

		} else if (event2.getX() - event1.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_THRESHOLD_VELOCITY) {
			
			int positionToRemove = mSentryItems.pointToPosition(
					(int) event1.getX(), (int) event1.getY());
			Repository repoItem = (Repository) mSentryAdapter
					.getItem(positionToRemove);
			
			SentryCreator creator = new SentryCreator(mContext, repoItem);
			creator.remove();
			
			mSentryAdapter.removeItem(positionToRemove);
			mSentryAdapter.notifyDataSetChanged();
			
			Toast.makeText(mContext,
					"Sentry to '" + repoItem.getRepositoryName() + "' cancelled",
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {

		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

}
