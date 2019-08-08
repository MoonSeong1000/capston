package com.example.test3;

import android.provider.BaseColumns;

public class DictionaryContract {
    public static final class DictionaryEntry implements BaseColumns {
        public static final String TABLE_NAME = "dictionarylist";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_phoneNum = "phoneNum";
        public static final String COLUMN_TIMESTAMP = "timestamp";

    }
}
