package com.rylow.eispbustracker_parentsedition.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by s.bakhti on 6.4.2016.
 */
public class Student implements Comparable<Student> {

    private String name;
    private String photo;
    private BusStop stop;
    private Assistant assistant;
    private Bus bus;
    private int rideid;
    private String ridedate;
    private Boolean rideinprogress;
    private String ridedirection;


    public Student(String name, String photo, BusStop stop, Assistant assistant, Bus bus, int rideid, String ridedate, Boolean rideinprogress, String ridedirection) {
        this.name = name;
        this.photo = photo;
        this.stop = stop;
        this.assistant = assistant;
        this.bus = bus;
        this.rideid = rideid;
        this.ridedate = ridedate;
        this.rideinprogress = rideinprogress;
        this.ridedirection = ridedirection;
    }

    @Override
    public String toString(){
        return name;
    }

    @Override
    public int compareTo(Student o) {
        return Integer.valueOf(this.stop.ridestopid).compareTo(o.getStop().getRidestopid());
    }

    public Boolean getRideinprogress() {
        return rideinprogress;
    }

    public String getRidedirection() {
        return ridedirection;
    }

    public void setRidedirection(String ridedirection) {
        this.ridedirection = ridedirection;
    }

    public void setRideinprogress(Boolean rideinprogress) {
        this.rideinprogress = rideinprogress;
    }

    public String getRidedate() {
        return ridedate;
    }

    public void setRidedate(String ridedate) {
        this.ridedate = ridedate;
    }

    public int getRideid() {
        return rideid;
    }

    public Assistant getAssistant() {
        return assistant;
    }

    public void setAssistant(Assistant assistant) {
        this.assistant = assistant;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public void setRideid(int rideid) {
        this.rideid = rideid;
    }

    public Bitmap getPhotoBitmap(){

        byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, o);

        int scale = 1;
        if (o.outHeight > 300 || o.outWidth > 300) {
            scale = (int)Math.pow(2, (int) Math.ceil(Math.log(200 /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        o2.inPurgeable=true;

        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, o2);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

   public BusStop getStop() {
        return stop;
    }

    public void setStop(BusStop stop) {
        this.stop = stop;
    }
}
