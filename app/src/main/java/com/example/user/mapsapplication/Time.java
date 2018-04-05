package com.example.user.mapsapplication;

import java.io.Serializable;

/**
 * Created by user on 11/21/2016.
 */
public class Time implements Serializable{

    String hour;
    String minute;
    String am_pm;

    Time()
    {
        hour="";
        minute="";
        am_pm="";

    }

    Time(String h,String m,String am_pm)
    {
        hour=h;
        minute=m;
        this.am_pm=am_pm;
    }

    public boolean isEmpty()
    {
        if(hour.isEmpty()&&minute.isEmpty()&&am_pm.isEmpty())
        {
            return true;
        }
        return false;
    }

    public void clear()
    {
        this.hour="";
        this.minute="";
        this.am_pm="";
    }

    public String getHour()
    {
        return  hour;
    }
    public String getMinute()
    {
        return minute;
    }

    public String getAm_pm()
    {
        return am_pm;
    }


    @Override
    public String toString() {

    return hour+","+minute+","+am_pm;



    }
}
