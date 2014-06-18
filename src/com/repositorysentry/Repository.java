package com.repositorysentry;

import java.util.Calendar;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import java.util.Date;

public abstract class Repository implements Parcelable {
	
	protected UUID mId;
	protected String mUsername;
	protected String mRepositoryName;
	protected String mDate;
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
	
	protected String getDate() {
		return mDate;
	}
	
	protected abstract String getUrl();
	
	protected abstract String getType();
	
	protected String getDateString() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int monthOfYear = c.get(Calendar.MONTH);
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

		// Increment monthOfYear for Calendar/Date -> Time Format setting
		monthOfYear++;
		String mon = "" + monthOfYear;
		String day = "" + dayOfMonth;

		if (monthOfYear < 10)
			mon = "0" + monthOfYear;
		if (dayOfMonth < 10)
			day = "0" + dayOfMonth;

		String dateString = day + "." + mon + "." + year;

		return dateString;
	}

}
