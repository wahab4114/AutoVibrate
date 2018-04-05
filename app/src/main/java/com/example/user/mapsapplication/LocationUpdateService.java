package com.example.user.mapsapplication;

import android.*;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import static android.support.v4.app.ActivityCompat.requestPermissions;


/**
 * Created by user on 11/16/2016.
 */
public class LocationUpdateService extends Service implements LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {


    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    Location location;

    private final int MY_PERMISSIONS_REQUEST_CODE = 1;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private final IBinder binder = new localBinder();

    private MapsActivity map;

    private  MainActivity main;

    private PinableLocation location_to_compare_with;

    int loccount;






    @Override
    public void onCreate() {


      //  Toast.makeText(getApplicationContext(), "onstartcommand", Toast.LENGTH_SHORT).show();




        // initializing API client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3 * 1000)        // 1 seconds, in milliseconds
                .setFastestInterval(3 * 1000); // 1 second, in milliseconds




    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Log.e("asd","-------------------------------->");

        loccount=0;

      //  Toast.makeText(getApplicationContext(), "onstartcommand", Toast.LENGTH_SHORT).show();

        location_to_compare_with = (PinableLocation) intent.getSerializableExtra("pinablelocation");


        if(!mGoogleApiClient.isConnected())
        {
      //      Toast.makeText(getApplicationContext(), "connect location service", Toast.LENGTH_SHORT).show();
            mGoogleApiClient.connect();
        }

        if(location_to_compare_with!=null)
        {
            Toast.makeText(getApplicationContext(), "StartSticky", Toast.LENGTH_SHORT).show();
            return START_STICKY;
        }


        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

      //  Toast.makeText(getApplicationContext(), "onBind", Toast.LENGTH_SHORT).show();

        return binder;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.i(TAG, "Location services connected-------------------------->.");

        //  if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        //  }



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {

            // location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
         //   Toast.makeText(getApplicationContext(), "req for updates", Toast.LENGTH_SHORT).show();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }



    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "Location services suspended. Please reconnect--------------------------->.");

      //  Toast.makeText(getApplicationContext(), "Location services suspended.", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onLocationChanged(Location location) {

        // Toast.makeText(getApplicationContext(), "long"+location.getLongitude()+"::lat"+location.getLatitude(), Toast.LENGTH_SHORT).show();

      /*  OnlocReceive r;
        r=
        r.onreceive(location);

        MapsActivity.*/


    if (map != null) {
        OnlocReceive listner = (OnlocReceive) map;
        listner.onreceive(location);
    }


    if (location_to_compare_with != null) {


        loccount++;

        if(loccount==10) {

            stopupdates();

            double x1, x2, y1, y2, distance;
            x1 = location_to_compare_with.getLatitude();
            y1 = location_to_compare_with.getLongitude();

            Location fakelocation = new Location("");
            fakelocation.setLatitude(x1);
            fakelocation.setLongitude(y1);

            distance = fakelocation.distanceTo(location);

            if (distance > location_to_compare_with.getRadius()) {


            } else {

                Toast.makeText(getApplicationContext(), "you are in location", Toast.LENGTH_SHORT).show();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.cast_ic_notification_1)
                        .setContentTitle("You are in marked location!")
                        .setContentText("your phone is in vibrate mode.");
                Intent intent = new Intent(this, MainActivity.class);
                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
                taskStackBuilder.addParentStack(MainActivity.class);
                taskStackBuilder.addNextIntent(intent);

                PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);

                builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

                //LED
                builder.setLights(Color.RED, 3000, 3000);


                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, builder.build());


                AudioManager am = (AudioManager) getBaseContext().getSystemService(AUDIO_SERVICE);

                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);


            }

            stopSelf();
        }




}
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        //    Toast.makeText(getApplicationContext(), "Location services connection failed.", Toast.LENGTH_SHORT).show();


    }

    public void stopupdates()
    {
           if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);



            mGoogleApiClient.disconnect();

         //      Toast.makeText(getApplicationContext(), "stop updates", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "inOnpause---------------------->.");

        }

    }

    public void startupdates()
    {

        mGoogleApiClient.connect();

    }


    // binder class

    public class localBinder extends Binder
    {
        public LocationUpdateService getservice()
        {
            return LocationUpdateService.this;
        }
    }


    void passClassrefrence(MapsActivity map)
    {
        this.map = map;
    }

    void passClassrefrence_of_main(MainActivity main){this.main = main;}





}
