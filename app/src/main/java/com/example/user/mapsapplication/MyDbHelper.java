package com.example.user.mapsapplication;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by user on 11/24/2016.
 */
public class MyDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME="myDB";
    public static final String TABLE_NAME="PinableLocationData";
    public static final String S_TIME="StartTime";
   // public static final String S_AM_PM="S_Am_OR_Pm";
    public static final String E_TIME="EndTime";
   // public static final String E_AM_PM="E_Am_OR_Pm";
    public static final String LATITUDE="Latitude";
    public static final String LONGITUDE="Longitude";
    public static final String RADIUS="Radius";
    public static final String LOC_NAME="Name";
    public static final int DATABASE_VERSION= 1;

    Context context;

    public MyDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        this.context=context;
    }




    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String sql= "CREATE TABLE "+TABLE_NAME +" ( "+S_TIME +" VARCHAR(50),"/*+S_AM_PM +" TEXT,"*/+E_TIME+" VARCHAR(50),"/*+E_AM_PM+" TEXT,"*/+
                LATITUDE +" VARCHAR(50),"+ LONGITUDE+" VARCHAR(50),"+RADIUS+" INTEGER,"+LOC_NAME+" VARCHAR(50));";

        try
        {
            sqLiteDatabase.execSQL(sql);
        }
        catch (SQLException e)
        {
            Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
