package com.android.solarcalculator.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    //db version
    private static final int DATABASE_VERSION = 1;
    //db name
    private static final String DATABASE_NAME = "location_db";
    //table name
    private static final String TABLE_NAME = "locations";

    private static final String KEY_ID = "id";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_LOCATION = "location";
    private static final String[] COLUMNS = { KEY_ID, KEY_LAT, KEY_LON, KEY_LOCATION};

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_table = "CREATE TABLE " + TABLE_NAME + " ( id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "lat TEXT, " + "lon TEXT, " + "location TEXT, " + "UNIQUE (lat, lon) )";
        db.execSQL(create_table);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public boolean addData(String latit, String longit, String location){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LAT,latit);
        values.put(KEY_LON,longit);
        values.put(KEY_LOCATION, location);

        Log.d(TAG, "addData: Adding " + latit + " " + longit + " " + location + " to " + TABLE_NAME);

//        long result = sqLiteDatabase.insert(TABLE_NAME,null, values);
        long result = sqLiteDatabase.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        if(result == -1){
            return false;
        }else {
            return true;
        }
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = " SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getd(String la, String lo){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE lat=? AND lon=?", new String[]{la, lo});
        return mCursor;

    }




}
