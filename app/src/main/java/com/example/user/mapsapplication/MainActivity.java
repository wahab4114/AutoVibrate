package com.example.user.mapsapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity{


    private AlarmManager mAlarmManager;
    private CallbackManager mCallbackManager;
    private ShareDialog shareDialog;
    ListView listView;
    ArrayList<PinableLocation> PinableLocationArrayList;
    FloatingActionButton fab;
    MyAdapter myAdapter;
    int request_code=1;
    RelativeLayout emptylayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);

        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        emptylayout = (RelativeLayout) findViewById(R.id.emptyview);
        fab = (FloatingActionButton) findViewById(R.id.fab);

       PinableLocationArrayList = new ArrayList<PinableLocation>();
        listView= (ListView) findViewById(R.id.list_view);



        myAdapter= new MyAdapter(this,PinableLocationArrayList);
        listView.setEmptyView(findViewById(R.id.emptyview2));

        mCallbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("Success", "Login");

                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(MainActivity.this, "Login Cancel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });


        //listView.setVisibility(View.INVISIBLE);

        //listView.setEmptyView(emptylayout);

        listView.setAdapter(myAdapter);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,MapsActivity.class);

                intent.putExtra("array",PinableLocationArrayList);


                startActivityForResult(intent,request_code);

            }
        });

        getDataFromDataBase();


        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            builALertMessageNoWifi();
        }


        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();



        }




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("in main's on resume","------------------------------------------------------------------------>");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


            if(requestCode==this.request_code)
            {
                if(resultCode==RESULT_OK)
                {
                    PinableLocation pinablelocation = (PinableLocation) data.getSerializableExtra("pinablelocation");

                    PinableLocationArrayList.add(pinablelocation);

                    myAdapter.notifyDataSetChanged();

                    addToDataBase(pinablelocation);

                    setAlarm(pinablelocation);






                }
            }


    }




    void setAlarm(PinableLocation pinableLocation)
    {


        Calendar calendar1 = Calendar.getInstance(); // calendar to get starting time in millis
        calendar1.setTimeInMillis(System.currentTimeMillis());



        Calendar calendar2 = Calendar.getInstance(); // calendar to get ending time in millis
        calendar2.setTimeInMillis(System.currentTimeMillis());


        Time stime=new Time();
        Time etime=new Time();


        Intent intent = new Intent(this,receiver.class);


        Intent intent1 = new Intent(this,receiver.class);




        PendingIntent pendingIntent;

        PendingIntent pendingIntent1;



        stime = pinableLocation.getStartTime();
        etime = pinableLocation.getEndTime();

        int stimehour = Integer.valueOf(stime.getHour());
        int stimemin = Integer.valueOf(stime.getMinute());

        int etimehour = Integer.valueOf(etime.getHour());
        int etimemin = Integer.valueOf(etime.getMinute());

        //  intent.putExtra("stime", stime.toString());
        intent.putExtra("pinablelocation",pinableLocation);
        intent.putExtra("which","start");

        intent1.putExtra("pinablelocation",pinableLocation);
        intent1.putExtra("which","end");





        if(stimehour!=12)
        {
            calendar1.set(Calendar.HOUR, Integer.valueOf(stime.getHour()));
        }
        else
        {
            calendar1.set(Calendar.HOUR, 0);
        }

        calendar1.set(Calendar.MINUTE, Integer.valueOf(stime.getMinute()));
        calendar1.set(Calendar.SECOND,0);
        calendar1.set(Calendar.MILLISECOND,0);
        if (stime.getAm_pm().equals("am")) {
            calendar1.set(Calendar.AM_PM, Calendar.AM);
        } else {
            calendar1.set(Calendar.AM_PM, Calendar.PM);
        }


        if(etimehour!=12)
        {
            calendar2.set(Calendar.HOUR, Integer.valueOf(etime.getHour()));
        }
        else
        {
            calendar2.set(Calendar.HOUR, 0);
        }

        calendar2.set(Calendar.MINUTE, Integer.valueOf(etime.getMinute()));
        calendar2.set(Calendar.SECOND,0);
        calendar2.set(Calendar.MILLISECOND,0);

        if(etime.getAm_pm().equals("am"))
        {
            calendar2.set(Calendar.AM_PM,Calendar.AM);
        }
        else
        {
            calendar2.set(Calendar.AM_PM,Calendar.PM);

        }

        Log.e("timeofcal1","h="+calendar1.get(Calendar.HOUR)+"m="+calendar1.get(Calendar.MINUTE)+"ampm="+calendar1.get(Calendar.AM_PM));


        Log.e("timeofcal2","h="+calendar2.get(Calendar.HOUR)+"m="+calendar2.get(Calendar.MINUTE)+"ampm="+calendar2.get(Calendar.AM_PM));




            double idx =  pinableLocation.getLatitude()*10000;
            double idy = pinableLocation.getLongitude()*15000;


        Log.e("cal1timeinMili",""+calendar1.getTimeInMillis()+"---------------------------------->");
        Log.e("cal2timeinMili",""+calendar2.getTimeInMillis()+"---------------------------------->");
        Log.e("currtimeinmili",""+System.currentTimeMillis()+"----------------------------------->");


            int id = (int) (idx+idy); // ids for all starting time;

            idx = pinableLocation.getLatitude()*20000;
            idy = pinableLocation.getLongitude()*25000;



            int id2 = (int) (idx+idy);



            //intent for all starting time alarms
            pendingIntent = PendingIntent.getBroadcast(this,id, intent,0) ;


            // intent for all ending time alarms
            pendingIntent1 = PendingIntent.getBroadcast(this,id2,intent1,0);





            if (Build.VERSION.SDK_INT >= 19) {
                // if(currtime.getTimeInMillis()<calendar1.getTimeInMillis()) {


                // set alarm for starting time of row
                mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar1.getTimeInMillis(), pendingIntent);

                //set alarm for ending time of row
                mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar2.getTimeInMillis(), pendingIntent1);



                Log.e("setalarm", "------------------------------------------------------------------------>");


            } else {


                mAlarmManager.set(AlarmManager.RTC_WAKEUP,calendar1.getTimeInMillis(), pendingIntent);


                //set alarm for ending time of row
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), pendingIntent1);

            }




    }



    void addToDataBase(PinableLocation pinableLocation)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyDbHelper.S_TIME,pinableLocation.getStartTime().toString());

        contentValues.put(MyDbHelper.E_TIME,pinableLocation.getEndTime().toString());

        contentValues.put(MyDbHelper.LATITUDE,pinableLocation.getLatitude().toString());

        contentValues.put(MyDbHelper.LONGITUDE,pinableLocation.getLongitude().toString());

        contentValues.put(MyDbHelper.RADIUS,pinableLocation.getRadius());

        contentValues.put(MyDbHelper.LOC_NAME,pinableLocation.getLocation());

       getContentResolver().insert(Uri.parse("content://com.example.user.mapsapplication/"+MyDbHelper.TABLE_NAME),contentValues);



    }

    void getDataFromDataBase()
    {


                String [] col_to_select = {MyDbHelper.S_TIME,MyDbHelper.E_TIME,MyDbHelper.LATITUDE,MyDbHelper.LONGITUDE,MyDbHelper.RADIUS,MyDbHelper.LOC_NAME};
                Cursor cursor = getContentResolver().query(Uri.parse("content://com.example.user.mapsapplication/"+MyDbHelper.TABLE_NAME),col_to_select,null,null,null);


                Log.e("cursercount",cursor.getCount()+"------------------------------------------------------>");

                Log.e("curser_col_count",cursor.getColumnCount()+"------------------------------------------------------>");



                while(cursor.moveToNext())
                {


                    Log.e("getdatafromdb","------------------------------------------------------>");

                    PinableLocation pinableLocation;
                    Time stime;
                    Time etime;
                   // double lat;
                   // double lng;

                    String lat;
                    String lng;

                    int rad;
                    String name;

                    List<String> timeList = Arrays.asList( cursor.getString(0).split(","));
                    stime = new Time(timeList.get(0),timeList.get(1),timeList.get(2));
                    timeList = Arrays.asList( cursor.getString(1).split(","));
                    etime = new Time(timeList.get(0),timeList.get(1),timeList.get(2));

                    /*lat=cursor.getDouble(2);
                    lng=cursor.getDouble(3);*/
                    lat=cursor.getString(2);
                    lng=cursor.getString(3);
                    rad=cursor.getInt(4);
                    name=cursor.getString(5);

                   // pinableLocation = new PinableLocation(stime,etime,lat,lng,rad,name);
                    pinableLocation = new PinableLocation(stime,etime,Double.valueOf(lat),Double.valueOf(lng),rad,name);


                    PinableLocationArrayList.add(pinableLocation);

                    myAdapter.notifyDataSetChanged();



                }




    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void builALertMessageNoWifi()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your WIFI seeems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

                    }
                })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_cart:
                final List<String> PUBLISH_PERMISSIONS = Arrays.asList("publish_actions");
                LoginManager.getInstance().logInWithPublishPermissions(MainActivity.this, PUBLISH_PERMISSIONS);
                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent content = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse("https://developers.facebook.com"))
                            .setContentTitle("My message ")
                            .build();
                    shareDialog.show(content);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int responseCode,    Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);
        mCallbackManager.onActivityResult(requestCode, responseCode, data);
    }


}
