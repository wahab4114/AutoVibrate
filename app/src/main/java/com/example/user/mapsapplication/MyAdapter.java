package com.example.user.mapsapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by user on 11/23/2016.
 */

public class MyAdapter extends BaseAdapter {

    ArrayList<PinableLocation> PinableLocationArray;
    Context context;

    RelativeLayout relativeLayout;

    LayoutInflater inflater;

    MyAdapter(Context c, ArrayList<PinableLocation> arrayList){

        inflater =  (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context=c;
        relativeLayout = (RelativeLayout)  inflater.inflate(R.layout.activity_main,null,false).findViewById(R.id.main);
        PinableLocationArray=arrayList;
    }



    @Override
    public int getCount() {

        return PinableLocationArray.size();
    }

    @Override
    public Object getItem(int i) {
        return PinableLocationArray.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View convertView, ViewGroup parent) {

        if( convertView == null ){
            //We must create a View:
            convertView = inflater.inflate(R.layout.my_adapter_layout, parent, false);

        }


        PinableLocation temp = PinableLocationArray.get(i);
        TextView STime = (TextView) convertView.findViewById(R.id.startTime);
        TextView SAmPm = (TextView) convertView.findViewById(R.id.StartAmPm);
        TextView ETime = (TextView) convertView.findViewById(R.id.endTime);
        TextView EAmPm = (TextView) convertView.findViewById(R.id.EndAmPm);
        TextView Loc = (TextView) convertView.findViewById(R.id.Location);

        ImageButton imageButton;
        ImageButton imageButton1;


        imageButton=(ImageButton) convertView.findViewById(R.id.imageButton);

        imageButton1=(ImageButton) convertView.findViewById(R.id.del);

        imageButton.setTag(i);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PinableLocation temp = PinableLocationArray.get((Integer) v.getTag());
                Intent intent = new Intent(context,MapsActivity.class);
                intent.putExtra("location",temp);
                context.startActivity(intent);


            }
        });


        imageButton1.setTag(i);
        Log.e("postag","pos="+i+"Tag="+i+"------------------------------------------------------------->");

        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {


                Integer index = (Integer)v.getTag();
                 PendingIntent[] pendingIntent = new PendingIntent[1];
                 PendingIntent[] pendingIntent1 = new PendingIntent[1];

                 PinableLocation pinableLocation = PinableLocationArray.get(index.intValue());

                String lng = pinableLocation.getLongitude().toString();
                String lat=pinableLocation.getLatitude().toString();


              /*  new Thread(new Runnable() {
                    @Override
                    public void run() {

                    }
                }).start();*/
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent,intent1;
                intent = new Intent(context,receiver.class);
                intent1 = new Intent(context,receiver.class);


                intent.putExtra("pinablelocation",pinableLocation);
                intent.putExtra("which","start");

                intent1.putExtra("pinablelocation",pinableLocation);
                intent1.putExtra("which","end");


                double idx =  pinableLocation.getLatitude()*10000;
                double idy = pinableLocation.getLongitude()*15000;

                int id = (int) (idx+idy); // ids for all starting time;

                idx = pinableLocation.getLatitude()*20000;
                idy = pinableLocation.getLongitude()*25000;



                int id2 = (int) (idx+idy);


                //intent for all starting time alarms
                pendingIntent[0] = PendingIntent.getBroadcast(context,id, intent,0) ;


                // intent for all ending time alarms
                pendingIntent1[0] = PendingIntent.getBroadcast(context,id2,intent1,0);



                alarmManager.cancel(pendingIntent[0]);
                alarmManager.cancel(pendingIntent1[0]);


                Log.e("alarm cancelled","////////////////////////////////////////////////////////");

                Log.e("tag",(Integer)v.getTag()+"////////////////////////////////////////////////////////");



               /* int deleted=context.getContentResolver().delete(Uri.parse("content://com.example.user.mapsapplication/"+MyDbHelper.TABLE_NAME),
                        "StartTime = ? AND EndTime = ? AND Latitude = ? AND Longitude = ?",new String[]{pinableLocation.getStartTime().toString(),
                                pinableLocation.getEndTime().toString(),pinableLocation.getLatitude().toString(),pinableLocation.getLongitude().toString()});
*/

               int deleted=context.getContentResolver().delete(Uri.parse("content://com.example.user.mapsapplication/"+MyDbHelper.TABLE_NAME),
                "Latitude = ? AND Longitude = ?",new String[]{lat,lng});



                if(deleted!=0) {


                    PinableLocationArray.remove(index.intValue());


                    notifyDataSetChanged();

                    Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();

/*Snackbar snackbar = Snackbar.make(relativeLayout,"deleted!",Snackbar.LENGTH_SHORT);
                    snackbar.show();*/

                    Toast.makeText(context, "" + deleted, Toast.LENGTH_SHORT).show();
                }
                    Toast.makeText(context, "" + deleted, Toast.LENGTH_SHORT).show();

            }





        });



        STime.setText(temp.getStartTime().getHour()+":"+temp.getStartTime().getMinute());
        SAmPm.setText(temp.getStartTime().getAm_pm());
        ETime.setText(temp.getEndTime().getHour()+":"+temp.getEndTime().getMinute());
        EAmPm.setText(temp.getEndTime().getAm_pm());
        Loc.setText(temp.getLocation());

        return convertView;
    }
}
