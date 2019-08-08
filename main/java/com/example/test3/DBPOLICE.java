package com.example.test3;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DBPOLICE {

    private String DATABASE_NAME = null;
    private String TABLE_NAME2 = "POLICE";
    private int DATABASE_VERSION = 1;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private String filePath;
    private String fileName;
    private Context context;
    private static DBPOLICE isDB;

    public static DBPOLICE setDB() {
        if (isDB == null) {
            isDB = new DBPOLICE();
        }
        return isDB;
    }


    public void isCheckDB(Context context, String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.context = context;

        File file = new File(this.filePath + "/" + fileName);
        File folder = new File(this.filePath);
        if (file.isFile()) {
            if (openDatabase()) {
                println("성공1");
            } else
                println("실패1");
        } else {
            folder.mkdirs();
            CopyDB();
            if (openDatabase()) {
                println("성공2");
            } else
                println("실패2");
        }
    }

    public void CopyDB() {
        AssetManager assetManager = context.getResources().getAssets();

        File outfile = new File(filePath + "/" + fileName);

        InputStream is = null;

        FileOutputStream fo = null;

        long filesize = 0;

        try {

            is = assetManager.open(fileName, AssetManager.ACCESS_BUFFER);
            filesize = is.available();

            if (outfile.length() <= 0) {
                byte[] tempdata = new byte[(int) filesize];
                is.read(tempdata);
                is.close();
                outfile.createNewFile();
                fo = new FileOutputStream(outfile);
                fo.write(tempdata);
                fo.close();
            }
        } catch (IOException e) {
        }
    }

    private boolean openDatabase() {
        DATABASE_NAME = filePath + "/" + fileName;
        println("opening database [" + DATABASE_NAME + "].");

        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getReadableDatabase();

        return true;
    }


    public static class POLICE {
        String name;
        String manageOffice;
        String kind;
        String tellNum;
        String addr;
        Double latitude;
        Double longitude;
        public POLICE(){}
        public POLICE(String manageOffice, String tellNum, String addr, double latitude, double longitude) {
            this.name = name;
            this.addr = addr;
            this.kind = kind;
            this.manageOffice = manageOffice;
            this.tellNum = tellNum;
            this.latitude = latitude;
            this.longitude = longitude;
        }
        public String getName() { return name; }
        public String getManageOffice() { return manageOffice; }
        public String getKind() { return kind; }
        public String getTellNum() { return tellNum; }
        public String getAddr() { return addr; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
    }

    public ArrayList searchPolice(double lat1, double lat2, double lng1, double lng2) {
        ArrayList<POLICE> polices = null;

        println("\nexecuteRawQueryParam called.\n");
        String latitude = "위도";
        String longitude = "경도";
        polices = new ArrayList<>();
        polices.clear();
        String SQL = "select * "
                + " from " + TABLE_NAME2
                + " where "+ latitude +  " between " + lat1 + " and " + lat2 + " and " +  longitude + " between " + lng1 + " and " + lng2;

        String[] args = {};
        Cursor c1 = db.rawQuery(SQL, args);

        for (int i = 0; i < c1.getCount(); i++) {
            c1.moveToNext();

            polices.add(new POLICE(
                    c1.getString(1), c1.getString(3) , c1.getString(4),
                    Double.parseDouble(c1.getString(5)), Double.parseDouble(c1.getString(6))));
        }

        c1.close();
        return polices;
    }


    private void println(String msg) {
        Log.d("DB", msg);
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE MESSAGE " +
                            "(id integer primary key autoincrement, " +
                            "name text," +
                            "phone_num text, " +
                            "contents text);");
        }



        public void onOpen(SQLiteDatabase db) {
            println("opened database [" + DATABASE_NAME + "].");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("DB", "Upgrading database from version " + oldVersion + " to " + newVersion + ".");
        }
    }
}