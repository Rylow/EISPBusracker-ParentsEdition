package com.rylow.eispbustracker_parentsedition;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rylow.eispbustracker_parentsedition.network.Connect;
import com.rylow.eispbustracker_parentsedition.network.TransmissionCodes;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by s.bakhti on 13.4.2016.
 */
public class BusLocationActivity extends AppCompatActivity {

    private static Timer refreshtimer;
    private TextView lblLatestUpdate;
    private MapView map;
    private IMapController mapController;
    private Button btnMapClose;
    private Drawable busStopMarker;
    private Drawable busMarker;
    private int rideid;
    private String busstopx;
    private String busstopy;
    private Boolean returnFromPause = false;

    private class ReloadTimerTask extends TimerTask {
        @Override
        public void run() {

            Thread.currentThread().setName("Refresh timer");

            Connect connect = Connect.getInstance();

            try{

                if (connect.getClientSocket().isClosed()){

                    if (connect.connect()){

                        BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(connect.getClientSocket().getOutputStream()));
                        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connect.getClientSocket().getInputStream()));

                        JSONObject json = new JSONObject();

                        json.put("code", TransmissionCodes.REQUEST_LAST_KNOWN_GPS);
                        json.put("rideid", rideid);

                        outToServer.write(json.toString());
                        outToServer.newLine();
                        outToServer.flush();

                        String incString = inFromServer.readLine();

                        if (incString != null) {
                            incString = incString.trim();
                        }
                        else {
                            incString = "";
                            connect.getClientSocket().close();

                        }

                        final JSONObject recievedJSON = new JSONObject(incString);

                        if (recievedJSON.getInt("code") == TransmissionCodes.RESPONSE_LAST_KNOWN_GPS) {

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    try {
                                        showLocation(recievedJSON.getDouble("gpsx"), recievedJSON.getDouble("gpsy"), recievedJSON.getString("time"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        }

                    }
                    else {


                    }
                }
                else{

                    BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(connect.getClientSocket().getOutputStream()));
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connect.getClientSocket().getInputStream()));

                    JSONObject json = new JSONObject();

                    json.put("code", TransmissionCodes.REQUEST_LAST_KNOWN_GPS);
                    json.put("rideid", rideid);

                    outToServer.write(json.toString());
                    outToServer.newLine();
                    outToServer.flush();

                    String incString = inFromServer.readLine();

                    if (incString != null) {
                        incString = incString.trim();
                    }
                    else {
                        incString = "";
                        connect.getClientSocket().close();

                    }

                    final JSONObject recievedJSON = new JSONObject(incString);

                    if (recievedJSON.getInt("code") == TransmissionCodes.RESPONSE_LAST_KNOWN_GPS) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                try {
                                    showLocation(recievedJSON.getDouble("gpsx"), recievedJSON.getDouble("gpsy"), recievedJSON.getString("time"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                    }

                }




            } catch (IOException e) {
                try {
                    connect.getClientSocket().close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }



        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_location);

        lblLatestUpdate = (TextView) findViewById(R.id.lblLastUpdate);
        map = (MapView) findViewById(R.id.mapBusLocation);
        mapController = map.getController();
        btnMapClose = (Button) findViewById(R.id.btnMapClose);
        busMarker = BusLocationActivity.this.getResources().getDrawable(R.drawable.bus_marker);
        busStopMarker = BusLocationActivity.this.getResources().getDrawable(R.drawable.busstop_marker2);

        mapController.setZoom(13);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        Intent recivedIntent = getIntent();

        Double gpsx = recivedIntent.getDoubleExtra("gpsx", 0.0);
        Double gpsy = recivedIntent.getDoubleExtra("gpsy", 0.0);
        final String time = recivedIntent.getStringExtra("time");
        rideid = recivedIntent.getIntExtra(("rideid"), 0);
        busstopx = recivedIntent.getStringExtra("busstopx");
        busstopy = recivedIntent.getStringExtra("busstopy");

        showLocation(gpsx, gpsy, time);

        btnMapClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(BusLocationActivity.this, RideInfoActivity.class);

                startActivity(intent);

                refreshtimer.cancel();

                finish();

            }
        });

        if (!returnFromPause) {

            Log.v("onCreate", "1111");
            refreshtimer = new Timer();

            refreshtimer.scheduleAtFixedRate(new ReloadTimerTask(), 500, 15000);
        }



    }

    private void showLocation(Double gpsx, Double gpsy, String time){


        lblLatestUpdate.setText("Latest update: " + time);


        GeoPoint startPoint = new GeoPoint(gpsx, gpsy);
        mapController.setCenter(startPoint);

        OverlayItem busStopPosition = new OverlayItem("Location", "Current bus location",
                new GeoPoint(Float.valueOf(busstopx), Float.valueOf(busstopy)));

        OverlayItem busPosition = new OverlayItem("Location", "Current bus location",
                new GeoPoint(gpsx, gpsy));

        busPosition.setMarker(busMarker);
        busStopPosition.setMarker(busStopMarker);

        ArrayList<OverlayItem> overlayItemArray  = new ArrayList<OverlayItem>();

        overlayItemArray.add(busStopPosition);
        overlayItemArray.add(busPosition);

        ItemizedIconOverlay<OverlayItem> itemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(BusLocationActivity.this, overlayItemArray, null);

        // Add the overlay to the MapView
        map.getOverlays().clear();
        map.getOverlays().add(itemizedIconOverlay);


    }

    @Override
    public void onBackPressed() {

        refreshtimer.cancel();

        Intent intent = new Intent(BusLocationActivity.this, RideInfoActivity.class);

        startActivity(intent);
        finish();

    }
    @Override
    public void onPause(){

        refreshtimer.cancel();
        returnFromPause = true;

        super.onPause();

    }

    @Override
    public void onResume(){

        if (returnFromPause) {
            Log.v("onResume", "1111");
            refreshtimer = new Timer();
            refreshtimer.scheduleAtFixedRate(new ReloadTimerTask(), 500, 15000);
        }

        super.onResume();
    }




}
