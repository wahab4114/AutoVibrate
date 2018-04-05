package com.example.user.mapsapplication;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by user on 11/21/2016.
 */
public class TimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {


    static int count=0;

    static boolean isstart=false;
    static boolean isending=false;

    TimeReceive listner;

    MapsActivity mMap;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        count++;


        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),this,hour,minute,false);
        if(count==1) {
            timePickerDialog.setMessage("Ending Time");
            isending=true;

        }
        else if(count==2)
        {
            timePickerDialog.setMessage("Starting Time");
            isstart=true;

            count=0;
        }




        return timePickerDialog;




    }

    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {

        mMap = (MapsActivity)getActivity();

        listner =(TimeReceive)mMap;

       // Log.e("time","hour:"+hourOfDay+"::minute:"+minute);

        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutee = c.get(Calendar.MINUTE);

      //  Log.e("timeofsystem","hour:"+hourOfDay+"::minute:"+minute);


        if(isstart)
        {
            mMap.passtime(hourOfDay, minute, true);
            isstart=false;
        }
        else if(isending)
        {
            mMap.passtime(hourOfDay, minute, false);
            isending=false;
        }







    }
}
