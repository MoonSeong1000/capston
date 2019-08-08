package com.example.test3;

import android.provider.BaseColumns;

public class Search_DictionaryContract {
    public static final class DictionaryEntry implements BaseColumns {
        public static final String TABLE_NAME = "searchlist";
        public static final String START_NAME = "start_name";
        public static final String END_NAME = "end_name";
        public static final String START_LAT = "start_lat";
        public static final String START_LON = "start_lon";
        public static final String END_LAT = "end_lat";
        public static final String END_LON = "end_lon";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
