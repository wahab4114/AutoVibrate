package com.example.user.mapsapplication;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Permission;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.Provider;
import java.util.ArrayList;


public class MapsActivity extends FragmentActivity  implements TimeReceive,OnlocReceive,OnMapReadyCallback, /* LocationListener,*/ GoogleMap.OnMarkerClickListener/*, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks*/ {

    private GoogleMap mMap;

    Marker m;


    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    Location location;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private final int MY_PERMISSIONS_REQUEST_CODE = 1;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public GoogleApiClient getmGoogleApiClient()
    {
        return mGoogleApiClient;
    }

    boolean bound = false;

    LocationUpdateService locationService;


    public ServiceConnection serviceConnection;

    Intent serviceIntent;

    PlaceAutocompleteFragment autocompleteFragment;

    AutocompleteFilter typeFilter;

    Time current_time=null;

    Time StartTime=null;

    Time EndTime=null;

    Location fakelocation;

    int zoom = 16;

    int radius=100;

    String curr_location_name="";

    ArrayList<PinableLocation> Pinnablelocations;

    boolean viewsingle_location=false;

    SeekBar seekBar;

    Circle previousCircle;

    Marker previousMarker;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newlayout);

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(1000);

        fab = (FloatingActionButton) findViewById(R.id.fabinMap);

        previousCircle = null;
        previousMarker = null;

        Log.i(TAG, "inOncreate---------------------->.");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        seekBar.setVisibility(View.INVISIBLE);

        fab.setVisibility(View.INVISIBLE);


        fakelocation = new Location("");


        StartTime  = new Time();
        EndTime = new Time();
        current_time = new Time();



        if(getIntent().getSerializableExtra("location")!=null)
        {
            viewsingle_location=true;
        }


       autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);



        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ------------------------>" + place.getName());

                fab.setVisibility(View.VISIBLE);


                seekBar.setVisibility(View.VISIBLE);


                LatLng latlong= place.getLatLng();

                curr_location_name = place.getName().toString();

                fakelocation.setLatitude(latlong.latitude);
                fakelocation.setLongitude(latlong.longitude);



               // buildAlertMessageConfirmation(fakelocation);
                handleLocationDynamicCircle(fakelocation);



                Toast.makeText(MapsActivity.this, "place:"+place.getName(), Toast.LENGTH_SHORT).show();






            }

            @Override
            public void onError(Status status) {

            }
        });

      /*  ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            builALertMessageNoWifi();
        }


        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();



        }*/

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                locationService = ((LocationUpdateService.localBinder)service).getservice();

                locationService.passClassrefrence(MapsActivity.this);

                bound=true;

             //   Toast.makeText(MapsActivity.this, "bound", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

                bound = false;

             //   Toast.makeText(MapsActivity.this, "not_bound", Toast.LENGTH_SHORT).show();

            }
        };




        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //    Toast.makeText(MapsActivity.this, "req permission", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_CODE);

            }

        }
        else
        {
            // location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(viewsingle_location==false) {

                serviceIntent = new Intent(MapsActivity.this, LocationUpdateService.class);
                startService(serviceIntent);

                bound = bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
            }
        }






    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       // Toast.makeText(MapsActivity.this, "onMapReady", Toast.LENGTH_SHORT).show();


        view_single_location();


        if(viewsingle_location==false) {

            handlelocation_from_main_list();


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    radius = progress;
                  // LatLng latLng= new LatLng(fakelocation.getLatitude(),fakelocation.getLongitude());
                    // drawCircleDynamic(latLng);
                    handleLocationDynamicCircle(fakelocation);

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });




            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    buildAlertMessageConfirmation(fakelocation);



                }
            });


            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

            mMap.getUiSettings().setAllGesturesEnabled(false);

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {


                    fab.setVisibility(View.VISIBLE);


                    seekBar.setVisibility(View.VISIBLE);

                    // Toast.makeText(MapsActivity.this, "clicked latlong:"+latLng, Toast.LENGTH_SHORT).show();

                    fakelocation.setLatitude(latLng.latitude);

                    fakelocation.setLongitude(latLng.longitude);

                    //drawCircleDynamic(latLng);
                      handleLocationDynamicCircle(fakelocation);


                   // buildAlertMessageConfirmation(fakelocation);


                }
            });
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        Log.d("map", "marker clicked------------->");
        String s = marker.getTitle();
      //  Toast.makeText(MapsActivity.this, s, Toast.LENGTH_SHORT).show();

        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
      /*  if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

            mGoogleApiClient.disconnect();

            Log.i(TAG, "inOnpause---------------------->.");

        }*/

        radius=100;

        if(serviceIntent!=null && bound ==true ) {
            locationService.stopupdates();
            unbindService(serviceConnection);
            stopService(serviceIntent);
            bound=false;



        }

        Log.i(TAG, "inOnpause---------------------->.");



    }


    @Override
    protected void onStop() {
        super.onStop();


        Log.i(TAG, "inOnStop---------------------->.");




    }

    private void handleNewLocation(Location location) {


        Log.i(TAG, "in handle new location------------------------------->.");

        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

       Toast.makeText(MapsActivity.this, "lat:"+currentLatitude+"  lang:"+currentLongitude, Toast.LENGTH_SHORT).show();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));


        mMap.getUiSettings().setAllGesturesEnabled(true);

        if(location.getProvider().isEmpty()) // if this is the selected user location then draw a circle around it
        {



            drawCircle(latLng);


        }

        else
        {

            MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        if(m!=null)
            m.remove();

        m =  mMap.addMarker(options);

        }



       // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }



    private void handleLocationDynamicCircle(Location location)
    {
        Log.i(TAG, "in handle new location------------------------------->.");

        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

      //  Toast.makeText(MapsActivity.this, "lat:"+currentLatitude+"  lang:"+currentLongitude, Toast.LENGTH_SHORT).show();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));


        mMap.getUiSettings().setAllGesturesEnabled(true);

        if(location.getProvider().isEmpty()) // if this is the selected user location then draw a circle around it
        {



            drawCircleDynamic(latLng);


        }



        // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void drawCircleDynamic(LatLng location)
    {
        if(previousCircle!=null && previousMarker!=null)
        {
            previousCircle.remove();
            previousMarker.remove();

        }

        CircleOptions options = new CircleOptions();
        options.center( location );
        //Radius in meters
        options.radius( radius );
        options.fillColor(0x44ff0000);
        options.strokeColor(  0xffff0000 );
        options.strokeWidth( 8 );


        // Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.center);


        Bitmap b = getBitmap(R.drawable.center);


        MarkerOptions markerOptions;

        if(b!=null)

        {
            markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(b)).anchor(0.5f, 1).position(location).title("selected location");

            previousMarker=mMap.addMarker(markerOptions);
        }

        previousCircle = mMap.addCircle(options);


    }


    private void drawCircle( LatLng location ) {

        if(previousCircle!=null && previousMarker!=null)
        {
            previousCircle.remove();
            previousMarker.remove();

            // this is because now when we select a new location after selecting a location then we donot want to delete circle of previously selected location
            previousMarker=null;
            previousMarker=null;

        }

        CircleOptions options = new CircleOptions();
        options.center( location );
        //Radius in meters
        options.radius( radius );
        options.fillColor(0x44ff0000);
        options.strokeColor(  0xffff0000 );
        options.strokeWidth( 8 );


      // Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.center);


        Bitmap b = getBitmap(R.drawable.center);


        MarkerOptions markerOptions;

        if(b!=null)

        {
            markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(b)).anchor(0.5f, 1).position(location).title("selected location");

            mMap.addMarker(markerOptions);
        }

        previousCircle = mMap.addCircle(options);






    }


    public  void refresh(View v)
    {
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onreceive(Location l) {
        handleNewLocation(l);


        if(serviceIntent!=null && bound==true ) {


            locationService.stopupdates();
            unbindService(serviceConnection);
            stopService(serviceIntent);
            bound=false;


            Toast.makeText(MapsActivity.this, "stop service", Toast.LENGTH_SHORT).show();
        }

    }

        @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == MY_PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)

            {
              //  Toast.makeText(MapsActivity.this, " granted " + grantResults[0] + "granted2" + grantResults[1], Toast.LENGTH_SHORT).show();

                try {
                  //  location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    if(viewsingle_location==false) {

                    //    Toast.makeText(MapsActivity.this, "in recieve request new location", Toast.LENGTH_SHORT).show();
                        serviceIntent = new Intent(MapsActivity.this, LocationUpdateService.class);
                        startService(serviceIntent);
                        bound = bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
                    }

                } catch (SecurityException e) {

                }



            }


        }
    }


    @Override
    protected void onResume() {

       // Toast.makeText(MapsActivity.this, "on Resume", Toast.LENGTH_SHORT).show();

        Log.e(TAG, "inOresume---------------------->.");


        radius=100;
      //  stopService(serviceIntent);

        super.onResume();

    }

    @Override
    protected void onDestroy() {


        if(serviceIntent!=null && bound ==true ) {
            locationService.stopupdates();
            unbindService(serviceConnection);
            stopService(serviceIntent);
            bound=false;



        }
        super.onDestroy();

    }

  /*  private void buildAlertMessageNoGps() {
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

    }*/

    private void buildAlertMessageConfirmation(final Location location)
    {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to select this location?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        DialogFragment timepicker = new TimePicker();

                        timepicker.show(getSupportFragmentManager(),"timePicker");



                        DialogFragment timepicker2 = new TimePicker();


                        timepicker2.show(getSupportFragmentManager(),"timePicker2");


                              // Log.e("StartTime=",""+StartTime.getHour()+":"+StartTime.getMinute()+" "+StartTime.getAm_pm());
                               // Log.e("EndingTime=",""+EndTime.getHour()+":"+EndTime.getMinute()+" "+EndTime.getAm_pm());
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



    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    // listner when time picker dialog is cliced starting and ending time will be delivered ti MapsActivity

    @Override
    public void passtime(int hourOfDay,int minute,boolean is_start_time) {

        current_time = convert24hourtoAm_Pm(hourOfDay,minute);

        if(is_start_time==false)
        {
            EndTime=new Time(current_time.getHour(),current_time.getMinute(),current_time.getAm_pm());
           // Log.e("Ending time=",""+EndTime.getHour()+":"+EndTime.getMinute()+" "+EndTime.getAm_pm());

            current_time.clear();
        }
        else
        {
            StartTime=new Time(current_time.getHour(),current_time.getMinute(),current_time.getAm_pm());
          //  Log.e("Start time=",""+StartTime.getHour()+":"+StartTime.getMinute()+" "+StartTime.getAm_pm());

            current_time.clear();
        }

        if(StartTime!=null && EndTime!=null)
        {
            if(!StartTime.isEmpty()&&!EndTime.isEmpty())
            {
             //   Log.e("Start time----->=",""+StartTime.getHour()+":"+StartTime.getMinute()+" "+StartTime.getAm_pm());
             //   Log.e("End time----->>e=",""+EndTime.getHour()+":"+EndTime.getMinute()+" "+EndTime.getAm_pm());

                handleNewLocation(fakelocation);


                Toast.makeText(MapsActivity.this, "radius="+radius, Toast.LENGTH_SHORT).show();

                PinableLocation pinableLocation = new PinableLocation(StartTime,EndTime,fakelocation.getLatitude(),fakelocation.getLongitude(),radius,curr_location_name);




                Intent intent = this.getIntent();

                intent.putExtra("pinablelocation",pinableLocation);

                this.setResult(RESULT_OK,intent);

                finish();


            }
        }



        Log.e("Enter location","----------------------------->");


    }

    void handlelocation_from_main_list()
    {
                //need to make async

                Pinnablelocations = (ArrayList<PinableLocation>) getIntent().getSerializableExtra("array");

                for (int i=0;i<Pinnablelocations.size();i++)
                {
                    Location fakelocation=new Location("");
                    fakelocation.setLatitude(Pinnablelocations.get(i).getLatitude());
                    fakelocation.setLongitude(Pinnablelocations.get(i).getLongitude());
                    radius=Pinnablelocations.get(i).getRadius();
                    handleNewLocation(fakelocation);
                }




    }

    void view_single_location()
    {
        PinableLocation location = (PinableLocation) getIntent().getSerializableExtra("location");
        if(location!=null) {
            autocompleteFragment.setUserVisibleHint(false);
            mMap.getUiSettings().setAllGesturesEnabled(false);
            Location fakelocation = new Location("");
            fakelocation.setLatitude(location.getLatitude());
            fakelocation.setLongitude(location.getLongitude());
            viewsingle_location = true;
            radius=location.getRadius();
            handleNewLocation(fakelocation);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();



        Log.e("onStart","------------------------->");
    }

   static public Time convert24hourtoAm_Pm(int hourOfDay, int minute)
    {
        String h="",am_pm="",m="";

        Time t;

        if(hourOfDay>12)
        {

            h=String.valueOf(hourOfDay-12);
            am_pm="pm";
          //  Log.e("entered","---------------------> >12");
        }
        else if(hourOfDay==0)
        {
            h=String.valueOf(12);
            am_pm="am";
           // Log.e("entered","---------------------> ==12");
        }
        else if(hourOfDay==12)
        {
            h=String.valueOf(12);
            am_pm="pm";
           // Log.e("entered","---------------------> ==12");

        }
        else if(hourOfDay<12)
        {
            h=String.valueOf(hourOfDay);
            am_pm="am";

        }

        m=String.valueOf(minute);


        if(m.length()==1)
        {
            String temp=m;
            m="";
            m=m+"0";
            m=m+temp;
        }

        if(h.length()==1)
        {
            String temp=h;
            h="";
            h=h+"0";
            h=h+temp;
        }

     //   Log.e("Converted time:","time="+h+":"+m+" "+am_pm);

        t= new Time(h,m,am_pm);

        return t;

    }


}
