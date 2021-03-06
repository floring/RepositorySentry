package com.repositorysentry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

	public final static String TABLE_NAME = "reposentry";
	public final static String _ID = "_id";
	public final static String REPOSITORY_COLUMN = "repository";
	public final static String NAME_COLUMN = "name";
	public final static String DATE_COLUMN = "date";
	public final static String MESSAGE_COLUMN = "message";
	public final static String REPOSITORY_ID_COLUMN = "repo_id";
	public final static String VCS_COLUMN = "vcs";

	final private static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
			+ " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ REPOSITORY_COLUMN + " TEXT NOT NULL, " + NAME_COLUMN
			+ " TEXT NOT NULL, " + DATE_COLUMN + " TEXT NOT NULL, "
			+ MESSAGE_COLUMN + " TEXT, " + REPOSITORY_ID_COLUMN
			+ " TEXT NOT NULL, " + VCS_COLUMN + " TEXT NOT NULL)";

	final private static String NAME = "reposentry_db.db";
	final private static Integer VERSION = 1;

	public DatabaseOpenHelper(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		// N / A
	}
}
