package com.example.user.mapsapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by user on 11/26/2016.
 */
public class receiver extends BroadcastReceiver {

    Context c;
    LocationUpdateService locationService;
    ServiceConnection serviceConnection;

    AlarmManager mAlarmManager;




    @Override
    public void onReceive(final Context context, Intent intent) {

        c=context;

        mAlarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        Log.e("runreceiver", "------------------------------------>");




        Calendar c1 = Calendar.getInstance();

        int currhour=c1.get(Calendar.HOUR_OF_DAY);
        int currmin=c1.get(Calendar.MINUTE);
        String curr_ampm;


        Time t=MapsActivity.convert24hourtoAm_Pm(currhour,currmin);
        currhour = Integer.valueOf(t.getHour());
        currmin = Integer.valueOf(t.getMinute());
        curr_ampm = t.getAm_pm();






        Intent intent1  = new Intent(context,LocationUpdateService.class);
        PinableLocation pinablelocation = (PinableLocation) intent.getSerializableExtra("pinablelocation");



        int locstarttimehour=  Integer.valueOf(pinablelocation.getStartTime().hour);
       int locstarttimemin= Integer.valueOf(pinablelocation.getStartTime().minute);


        int locendtimehour= Integer.valueOf(pinablelocation.getEndTime().hour);
        int locendtimemin= Integer.valueOf(pinablelocation.getEndTime().minute);


        SharedPreferences sharedPreferences = c.getSharedPreferences(pinablelocation.getStartTime().toString()+pinablelocation.getEndTime().toString(),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
/*
       Log.e("currtime","hour:"+currhour+"  Min:"+currmin+" "+curr_ampm);
        Log.e("locstime","hour:"+locstarttimehour+"  Min:"+locstarttimemin+" "+pinablelocation.getStartTime().getAm_pm() );

       Log.e("locetime","hour:"+locstarttimehour+"  Min:"+locstarttimemin);
*/

// this check is to avoid start of service when starting time is an old one
       if(   (currhour==locstarttimehour)  &&  (currmin == locstarttimemin)  && (curr_ampm.equals(pinablelocation.getStartTime().getAm_pm()))  )
       {

           if (pinablelocation != null) {

               setRepeatingStartTimeAlarm(pinablelocation);
             //  if (c1.getTimeInMillis() < intent.getDoubleExtra("stimeinmillis", 0)) {
                   Log.e("starttime alarm invoked", "------------------------------------>");
                   intent1.putExtra("pinablelocation", pinablelocation);
                   context.startService(intent1);
             //  }
           }
      }

        else if( (currhour== locendtimehour)  &&  (currmin== locendtimemin)  && (curr_ampm.equals(pinablelocation.getEndTime().getAm_pm()))  )
        {


            if (pinablelocation != null)
            {

                String isOld=sharedPreferences.getString("isOld","false");
                if(isOld.equals("yes"))
                {
                    editor.putString("isOld","false");
                    editor.commit();
                    Toast.makeText(context, "setfornextDay2whenfirstisOld", Toast.LENGTH_SHORT).show();
                    setEndAlarmInCaseOfOldTime(pinablelocation);
                }
                else
                {

                    AudioManager am = (AudioManager) context.getSystemService(c.AUDIO_SERVICE);

                    am.setRingerMode(AudioManager.MODE_RINGTONE);

                    setRepeatingEndingAlarm(pinablelocation);
                }

                //  if (c1.getTimeInMillis() < intent.getDoubleExtra("stimeinmillis", 0)) {
               /* intent1.putExtra("pinablelocation", pinablelocation);
                context.startService(intent1);*///  }
            }

        }
        else
       {


           String which = intent.getStringExtra("which");

           if(which.equals("start"))
           {

               editor.putString("isOld","yes");
               editor.commit();
               Toast.makeText(context, "setfornextDay1", Toast.LENGTH_SHORT).show();
               setStartAlarmInCaseOfOldTime(pinablelocation);
           }
           else if(which.equals("end"))
           {
               editor.putString("isOld","false");
               editor.commit();
               Toast.makeText(context, "setfornextDay2", Toast.LENGTH_SHORT).show();
               setEndAlarmInCaseOfOldTime(pinablelocation);
           }



       }






    }


    void setRepeatingStartTimeAlarm(PinableLocation pinableLocation)
    {
        Calendar calendar1 = Calendar.getInstance(); // calendar to get starting time in millis
        calendar1.setTimeInMillis(System.currentTimeMillis());
        Time stime=new Time();


        Intent intent = new Intent(c,receiver.class);

        PendingIntent pendingIntent;

        stime = pinableLocation.getStartTime();
        int stimehour = Integer.valueOf(stime.getHour());
        int stimemin = Integer.valueOf(stime.getMinute());


        intent.putExtra("pinablelocation",pinableLocation);
        intent.putExtra("which","start");

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


        double idx =  pinableLocation.getLatitude()*10000;
        double idy = pinableLocation.getLongitude()*15000;


        Log.e("cal1timeinMili",""+calendar1.getTimeInMillis()+"---------------------------------->");
        Log.e("currtimeinmili",""+System.currentTimeMillis()+"----------------------------------->");


        int id = (int) (idx+idy); // ids for all starting time;


        //intent for all starting time alarms
        pendingIntent = PendingIntent.getBroadcast(c,id, intent,0) ;
        mAlarmManager.cancel(pendingIntent);


        if (Build.VERSION.SDK_INT >= 19) {
            // if(currtime.getTimeInMillis()<calendar1.getTimeInMillis()) {


            // set alarm for starting time of row
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+AlarmManager.INTERVAL_DAY, pendingIntent);


            Log.e("setalarmstartInReceiver", "------------------------------------------------------------------------>");


        } else {


            mAlarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+AlarmManager.INTERVAL_DAY, pendingIntent);


        }




    }



    void setRepeatingEndingAlarm(PinableLocation pinableLocation)
    {

        Calendar calendar2 = Calendar.getInstance(); // calendar to get ending time in millis
        calendar2.setTimeInMillis(System.currentTimeMillis());


        Time etime=new Time();


        Intent intent1 = new Intent(c,receiver.class);






        PendingIntent pendingIntent1;


        etime = pinableLocation.getEndTime();
        int etimehour = Integer.valueOf(etime.getHour());
        int etimemin = Integer.valueOf(etime.getMinute());






        //  intent.putExtra("stime", stime.toString());
        intent1.putExtra("pinablelocation",pinableLocation);
        intent1.putExtra("which","start");





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




      double idx = pinableLocation.getLatitude()*20000;
        double idy = pinableLocation.getLongitude()*25000;

        int id2 = (int) (idx+idy);


        // intent for all ending time alarms
        pendingIntent1 = PendingIntent.getBroadcast(c,id2,intent1,0);

        mAlarmManager.cancel(pendingIntent1);


        if (Build.VERSION.SDK_INT >= 19) {
            // if(currtime.getTimeInMillis()<calendar1.getTimeInMillis()) {


            //set alarm for ending time of row
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+AlarmManager.INTERVAL_DAY, pendingIntent1);



            Log.e("setAlarmEndInReceiver", "------------------------------------------------------------------------>");


        } else {


            //set alarm for ending time of row
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+AlarmManager.INTERVAL_DAY, pendingIntent1);

        }


    }



    void setStartAlarmInCaseOfOldTime(PinableLocation pinableLocation)
    {

        Calendar calendar1 = Calendar.getInstance(); // calendar to get starting time in millis
        calendar1.setTimeInMillis(System.currentTimeMillis());
        Time stime=new Time();


        Intent intenT = new Intent(c,receiver.class);

        PendingIntent pendingIntent;

        stime = pinableLocation.getStartTime();
        int stimehour = Integer.valueOf(stime.getHour());
        int stimemin = Integer.valueOf(stime.getMinute());


        intenT.putExtra("pinablelocation",pinableLocation);
        intenT.putExtra("which","start");

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


        double idx =  pinableLocation.getLatitude()*10000;
        double idy = pinableLocation.getLongitude()*15000;


        Log.e("cal1timeinMili",""+calendar1.getTimeInMillis()+"---------------------------------->");
        Log.e("currtimeinmili",""+System.currentTimeMillis()+"----------------------------------->");


        int id = (int) (idx+idy); // ids for all starting time;


        //intent for all starting time alarms
        pendingIntent = PendingIntent.getBroadcast(c,id, intenT,0) ;

        mAlarmManager.cancel(pendingIntent);


        if (Build.VERSION.SDK_INT >= 19) {
            // if(currtime.getTimeInMillis()<calendar1.getTimeInMillis()) {


            // set alarm for starting time of row
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar1.getTimeInMillis()+AlarmManager.INTERVAL_DAY, pendingIntent);


            Log.e("setalarmstartInReceiver", "------------------------------------------------------------------------>");


        } else {


            mAlarmManager.set(AlarmManager.RTC_WAKEUP,calendar1.getTimeInMillis()+AlarmManager.INTERVAL_DAY, pendingIntent);


        }
    }

    void setEndAlarmInCaseOfOldTime(PinableLocation pinableLocation)
    {
        Calendar calendar2 = Calendar.getInstance(); // calendar to get ending time in millis
        calendar2.setTimeInMillis(System.currentTimeMillis());


        Time etime=new Time();


        Intent intent1 = new Intent(c,receiver.class);






        PendingIntent pendingIntent1;


        etime = pinableLocation.getEndTime();
        int etimehour = Integer.valueOf(etime.getHour());
        int etimemin = Integer.valueOf(etime.getMinute());






        //  intent.putExtra("stime", stime.toString());
        intent1.putExtra("pinablelocation",pinableLocation);
        intent1.putExtra("which","start");





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




        double idx = pinableLocation.getLatitude()*20000;
        double idy = pinableLocation.getLongitude()*25000;

        int id2 = (int) (idx+idy);


        // intent for all ending time alarms
        pendingIntent1 = PendingIntent.getBroadcast(c,id2,intent1,0);

        mAlarmManager.cancel(pendingIntent1);


        if (Build.VERSION.SDK_INT >= 19) {
            // if(currtime.getTimeInMillis()<calendar1.getTimeInMillis()) {


            //set alarm for ending time of row
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar2.getTimeInMillis()+AlarmManager.INTERVAL_DAY, pendingIntent1);



            Log.e("setAlarmEndInReceiver", "------------------------------------------------------------------------>");


        } else {


            //set alarm for ending time of row
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis()+AlarmManager.INTERVAL_DAY, pendingIntent1);

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


        Intent intent = new Intent(c,receiver.class);




        PendingIntent pendingIntent;

        PendingIntent pendingIntent1;



        stime = pinableLocation.getStartTime();
        etime = pinableLocation.getEndTime();






        //  intent.putExtra("stime", stime.toString());
        intent.putExtra("pinablelocation",pinableLocation);




        calendar1.set(Calendar.HOUR, Integer.valueOf(stime.getHour()));
        calendar1.set(Calendar.MINUTE, Integer.valueOf(stime.getMinute()));





        calendar2.set(Calendar.HOUR, Integer.valueOf(etime.getHour()));
        calendar2.set(Calendar.MINUTE, Integer.valueOf(etime.getMinute()));



        if (stime.getAm_pm().equals("am")) {
            calendar1.set(Calendar.AM_PM, Calendar.AM);
        } else {
            calendar1.set(Calendar.AM_PM, Calendar.PM);
        }



        if(etime.getAm_pm().equals("am"))
        {
            calendar2.set(Calendar.AM_PM,Calendar.AM);
        }
        else
        {
            calendar2.set(Calendar.AM_PM,Calendar.PM);
        }



        double idx =  pinableLocation.getLatitude()*10000;
        double idy = pinableLocation.getLongitude()*15000;

        Log.e("cal1timeinMili",""+calendar1.getTimeInMillis()+"---------------------------------->");
        Log.e("cal1timeinMili",""+calendar2.getTimeInMillis()+"---------------------------------->");
        Log.e("currtimeinmili",""+System.currentTimeMillis()+"----------------------------------->");

        int id = (int) (idx+idy); // ids for all starting time;

        idx = pinableLocation.getLatitude()*20000;
        idy = pinableLocation.getLongitude()*25000;

        int id2 = (int) (idx+idy);



        //intent for all starting time alarms
        pendingIntent = PendingIntent.getBroadcast(c,id, intent,0) ;

        mAlarmManager.cancel(pendingIntent);


        // intent for all ending time alarms
        pendingIntent1 = PendingIntent.getBroadcast(c,id2,intent,0);

        mAlarmManager.cancel(pendingIntent1);


        if (Build.VERSION.SDK_INT >= 19) {
            // if(currtime.getTimeInMillis()<calendar1.getTimeInMillis()) {


            // set alarm for starting time of row
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar1.getTimeInMillis()+AlarmManager.INTERVAL_DAY, pendingIntent);

            //set alarm for ending time of row
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar2.getTimeInMillis()+AlarmManager.INTERVAL_DAY, pendingIntent1);



            Log.e("setalarm", "------------------------------------------------------------------------>");


        } else {


            mAlarmManager.set(AlarmManager.RTC_WAKEUP,calendar1.getTimeInMillis()+AlarmManager.INTERVAL_DAY, pendingIntent);


            //set alarm for ending time of row
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis()+AlarmManager.INTERVAL_DAY, pendingIntent1);

        }




    }






}
