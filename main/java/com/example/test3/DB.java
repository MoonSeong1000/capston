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

public class DB {

    private String DATABASE_NAME = null;
    private String TABLE_NAME = "seoul";
//    private String TABLE_NAME2 = "POLICE";
    private int DATABASE_VERSION = 1;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private String filePath;
    private String fileName;
    private Context context;
    private static DB isDB;

    public static DB setDB() {
        if (isDB == null) {
            isDB = new DB();
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

    public static class CCTV {
        String addr;
        String manageOffice;
        String tellNum;
        double latitude;
        double longitude;
        double d_heuristic;
        double map_distance;
        boolean state=false;

        public CCTV(){}
        public CCTV(String addr,
             String manageOffice,
             String tellNum,
             double latitude,
             double longitude) {
            this.addr = addr;
            this.manageOffice = manageOffice;
            this.tellNum = tellNum;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getAddr() {
            return addr;
        }

        public String getManageOffice() {
            return manageOffice;
        }

        public String getTellNum() {
            return tellNum;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setD_heuristic(double distance) {d_heuristic = distance;}

        public double getD_heuristic() {return d_heuristic;}

        public void setMap_distance(double m_distance) {map_distance = m_distance;}

        public double getMap_distance() {return map_distance;}
    }

    public ArrayList navirouteCCTV(double lat1, double lat2, double lng1, double lng2) {
        ArrayList<CCTV> CCTVs = null;

        println("\nexecuteRawQueryParam called.\n");

        CCTVs = new ArrayList<>();
        CCTVs.clear();
        String SQL = "select * "
                + " from " + TABLE_NAME
                + " where latitude between " + lat1 + " and " + lat2 + " and longitude between " + lng1 + " and " + lng2;

        Cursor c1 = db.rawQuery(SQL,null);
        if(c1 == null) {
            return CCTVs;
        }


        for (int i = 0; i < c1.getCount(); i++) {
            c1.moveToNext();

            CCTVs.add(new CCTV(
                    c1.getString(2), c1.getString(3) , c1.getString(5),
                    Double.parseDouble(c1.getString(7)), Double.parseDouble(c1.getString(8))));
            Collections.sort(CCTVs, sortByTotalCall);
        }
        c1.close();
        return CCTVs;
    }


    private final static Comparator<CCTV> sortByTotalCall = new Comparator<CCTV>() {
        @Override
        public int compare(CCTV c1, CCTV c2) {
            return Double.compare(c1.getLatitude(), c2.getLatitude());
        }
    };


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