package com.example.test3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Search_DictionaryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "searchdictionary.db";
    private static final int DATABASE_VERSION = 1;

    public Search_DictionaryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WAITLIST_TABLE = "CREATE TABLE " + Search_DictionaryContract.DictionaryEntry.TABLE_NAME + " (" +
                Search_DictionaryContract.DictionaryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Search_DictionaryContract.DictionaryEntry.START_NAME + " TEXT NOT NULL, " +
                Search_DictionaryContract.DictionaryEntry.END_NAME + " TEXT NOT NULL, " +
                Search_DictionaryContract.DictionaryEntry.START_LAT + " TEXT NOT NULL, " +
                Search_DictionaryContract.DictionaryEntry.START_LON + " TEXT NOT NULL," +
                Search_DictionaryContract.DictionaryEntry.END_LAT + " TEXT NOT NULL," +
                Search_DictionaryContract.DictionaryEntry.END_LON + " TEXT NOT NULL" +
                "); ";

        // 쿼리 실행
        sqLiteDatabase.execSQL(SQL_CREATE_WAITLIST_TABLE);
    }

    // DB 스키마가 최근 것을 반영하게 해준다.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // 버전이 바뀌면 예전 버전의 테이블을 삭제 (나중에 ALTER 문으로 대체)
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Search_DictionaryContract.DictionaryEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
