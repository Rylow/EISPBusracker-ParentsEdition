package com.rylow.eispbustracker_parentsedition.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by s.bakhti on 10.4.2016.
 */
public class Bus {

    private String spz;
    private String photo;

    public Bus(String spz, String photo) {
        this.spz = spz;
        this.photo = photo;
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

    public String getSpz() {
        return spz;
    }

    public void setSpz(String spz) {
        this.spz = spz;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
