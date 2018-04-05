package com.example.user.mapsapplication;

import java.io.Serializable;

/**
 * Created by user on 11/23/2016.
 */
public class PinableLocation implements Serializable{

    private Time StartTime;
    private Time EndTime;
    private double Latitude;
    private double Longitude;
    private int radius;
    private String Location;
    private int id;

    public boolean isReset;
    PinableLocation(Time t1,Time t2,double x,double y,int r,String loc){

        id=-1;
        isReset=false;
        StartTime=t1;
        EndTime=t2;
        Latitude=x;
        Longitude=y;
        radius=r;
        Location=loc;
    }

    public void setid(int id)
    {
        this.id=id;
    }
    public int getid()
    {
        return this.id;
    }


    public Time getStartTime(){
        return StartTime;
    }
    public Time getEndTime(){
        return EndTime;
    }
    public Double getLatitude(){
        return Latitude;
    }
    public Double getLongitude(){
        return Longitude;
    }
    public String getLocation(){
        return Location;
    }
    public int getRadius(){return radius;}

}


