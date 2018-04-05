package com.example.user.mapsapplication;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by user on 11/24/2016.
 */
public class LocationTimeContentProvider extends ContentProvider {

    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        matcher.addURI("com.example.user.mapsapplication","PinableLocationData",1);
    }

    private MyDbHelper dbHelper;
    private SQLiteDatabase db;



    @Override
    public boolean onCreate() {
        dbHelper = new MyDbHelper(getContext());
        db = dbHelper.getWritableDatabase();


        Log.e("created db","------------------------------------------------------>");

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if(matcher.match(uri) == 1){
            Log.e("get data from db","------------------------------------------------------>");
             return db.query("PinableLocationData",projection,null,null,null,null,null);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {


        if(matcher.match(uri)==1)
        {

            db.insert("PinableLocationData",null,contentValues);

            Log.e("Inserted","------------------------------------------------------>");

            return  uri;

        }


        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {



           return   db.delete(MyDbHelper.TABLE_NAME,s,strings);
            /*db.delete(MyDbHelper.TABLE_NAME,"StartTime="+"'"+strings[0]+"' AND "+
            "EndTime="+"'"+strings[1]+"' AND "+
                    "Latitude="+"'"+strings[2]+"' AND "+
                    "Longitude="+"'"+strings[3]+"'",null);*/
     /*     db.execSQL("DELETE FROM PinableLocationData WHERE StartTime="+"'"+strings[0]+"' AND "+
                                                            "EndTime="+"'"+strings[1]+"' AND "+
                                                            "Latitude="+"'"+strings[2]+"' AND "+
                                                            "Longitude="+"'"+strings[3]+"'"


           );*/

      /*  Double lat= Double.valueOf(strings[0]);
        Double lng =Double.valueOf(strings[1]);

        db.execSQL("DELETE FROM PinableLocationData WHERE Latitude="+lat+" AND "+
                "Longitude="+lng );


        return -1;*/

    }



    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
