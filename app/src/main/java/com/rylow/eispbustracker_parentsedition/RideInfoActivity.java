package com.rylow.eispbustracker_parentsedition;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rylow.eispbustracker_parentsedition.network.Connect;
import com.rylow.eispbustracker_parentsedition.network.TransmissionCodes;
import com.rylow.eispbustracker_parentsedition.service.Assistant;
import com.rylow.eispbustracker_parentsedition.service.Bus;
import com.rylow.eispbustracker_parentsedition.service.BusStop;
import com.rylow.eispbustracker_parentsedition.service.NoKidsActivity;
import com.rylow.eispbustracker_parentsedition.service.Student;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by s.bakhti on 10.4.2016.
 */
public class RideInfoActivity extends AppCompatActivity {

    private ImageView imgStudent, imgAssistant, imgBus, imgArrowLeft, imgArrowRight, imgBusStop;
    private TextView lblStudentName, lblRideDate, lblBoardTime, lblNextTime, lblBoardTimeLabel, lblBus, lblAssistant, lblStop, lblRideDateLabel;
    private int studentSelector;
    private Button btnShowMap;
    private Student selectedStudent;
    private static Timer refreshtimer;
    private Boolean returnFromPause = false;

    private class ReloadTimerTask extends TimerTask {
        @Override
        public void run() {

            Thread.currentThread().setName("Refresh timer");

            Connect connect = Connect.getInstance();

            try {

                if (connect.getClientSocket().isClosed()) {

                    if (connect.connect()) {

                        BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(connect.getClientSocket().getOutputStream()));
                        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connect.getClientSocket().getInputStream()));

                        JSONObject json = new JSONObject();

                        json.put("code", TransmissionCodes.REQUEST_RIDE_IN_TRANSIT_STATUS);
                        json.put("rideid", selectedStudent.getRideid());

                        outToServer.write(json.toString());
                        outToServer.newLine();
                        outToServer.flush();

                        String incString = inFromServer.readLine();

                        if (incString != null) {
                            incString = incString.trim();
                        } else {
                            incString = "";
                            connect.getClientSocket().close();

                        }

                        final JSONObject recievedJSON = new JSONObject(incString);

                        if (recievedJSON.getInt("code") == TransmissionCodes.RESPONSE_RIDE_IN_TRANSIT_STATUS) {

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    try {

                                        if (recievedJSON.getBoolean("rideinprogress")){
                                            btnShowMap.setVisibility(View.VISIBLE);
                                            setMapOnClickListner(btnShowMap);
                                        }
                                        else {
                                            btnShowMap.setVisibility(View.INVISIBLE);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        }

                    } else {


                    }
                } else {

                    BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(connect.getClientSocket().getOutputStream()));
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connect.getClientSocket().getInputStream()));

                    JSONObject json = new JSONObject();

                    json.put("code", TransmissionCodes.REQUEST_RIDE_IN_TRANSIT_STATUS);
                    json.put("rideid", selectedStudent.getRideid());

                    outToServer.write(json.toString());
                    outToServer.newLine();
                    outToServer.flush();

                    String incString = inFromServer.readLine();

                    if (incString != null) {
                        incString = incString.trim();
                    } else {
                        incString = "";
                        connect.getClientSocket().close();

                    }

                    final JSONObject recievedJSON = new JSONObject(incString);

                    if (recievedJSON.getInt("code") == TransmissionCodes.RESPONSE_RIDE_IN_TRANSIT_STATUS) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                try {

                                    if (recievedJSON.getBoolean("rideinprogress")){
                                        btnShowMap.setVisibility(View.VISIBLE);
                                        setMapOnClickListner(btnShowMap);
                                    }
                                    else {
                                        btnShowMap.setVisibility(View.INVISIBLE);
                                    }
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
        setContentView(R.layout.activity_ride_info);


        imgStudent = (ImageView) findViewById(R.id.imgStudentPhoto);
        imgAssistant = (ImageView) findViewById(R.id.imgAssistant);
        imgBus = (ImageView) findViewById(R.id.imgBus);
        imgBusStop = (ImageView) findViewById(R.id.imgBusStop);
        imgArrowLeft = (ImageView) findViewById(R.id.imgArrowLeft);
        imgArrowRight = (ImageView) findViewById(R.id.imgArrowRight);

        lblStudentName = (TextView) findViewById(R.id.lblStudentName);
        lblBoardTime = (TextView) findViewById(R.id.lblBoardTime);
        lblRideDate = (TextView) findViewById(R.id.lblRideDate);
        lblRideDateLabel = (TextView) findViewById(R.id.lblDateLabel);
        lblStop = (TextView) findViewById(R.id.lblStop);
        lblAssistant = (TextView) findViewById(R.id.lblAssistant);
        lblBus = (TextView) findViewById(R.id.lblBus);
        lblNextTime = (TextView) findViewById(R.id.lblNextRideLabel);
        lblBoardTimeLabel = (TextView) findViewById(R.id.lblBoardTimeLabel);

        btnShowMap = (Button) findViewById(R.id.btnShowMap);

        setMapOnClickListner(btnShowMap);

        if (!returnFromPause) {

            refreshtimer = new Timer();

            refreshtimer.scheduleAtFixedRate(new ReloadTimerTask(), 10000, 15000);
        }

        doTheJob();



    }

    private void setMapOnClickListner(Button btnShowMap) {

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AsyncTask query = new AsyncTask<Integer, Void, JSONObject>(){

                    @Override
                    protected JSONObject doInBackground(Integer... params) {

                        Connect connect = Connect.getInstance();


                        if (connect.getClientSocket().isClosed()){

                            if (connect.connect()){

                                return getLatestBusCoordinates(connect);

                            }
                            else {

                                showErrorMessage();

                                JSONObject returnJSON = null;
                                try {
                                    returnJSON = new JSONObject().put("code", 0);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                return returnJSON;


                            }
                        }
                        else{

                            return getLatestBusCoordinates(connect);

                        }
                    }
                }.execute();

                try {

                    JSONObject recievedJSON = (JSONObject) query.get();

                    Intent intent = new Intent(RideInfoActivity.this, BusLocationActivity.class);
                    intent.putExtra("gpsx", recievedJSON.getDouble("gpsx"));
                    intent.putExtra("gpsy", recievedJSON.getDouble("gpsy"));
                    intent.putExtra("time", recievedJSON.getString("time"));
                    intent.putExtra("rideid", selectedStudent.getRideid());
                    intent.putExtra("busstopx", selectedStudent.getStop().getGpsx());
                    intent.putExtra("busstopy", selectedStudent.getStop().getGpsy());

                    startActivity(intent);
                    finish();



                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    @Override
    public void onBackPressed() {

        refreshtimer.cancel();

        Intent intent = new Intent(RideInfoActivity.this, LoginActivity.class);

        try {
            Connect.getInstance().getClientSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        startActivity(intent);
        finish();

    }


    private void doTheJob(){

        final List<Student> listStudents;

        AsyncTask query = new AsyncTask<Integer, Void, List<Student>>(){

            @Override
            protected List<Student> doInBackground(Integer... params) {

                Connect connect = Connect.getInstance();


                if (connect.getClientSocket().isClosed()){

                    if (connect.connect()){

                        return getStudentInfo(connect);

                    }
                    else {

                        showErrorMessage();

                        return new ArrayList<>();


                    }
                }
                else{

                    return getStudentInfo(connect);

                }
            }
        }.execute();

        try {
            listStudents = (List<Student>) query.get();

            if (listStudents.size() > 0){

                setViewInfo(listStudents.get(0));

                studentSelector = 0;

                if (listStudents.size() > 1){

                    imgArrowRight.setVisibility(View.VISIBLE);
                    imgArrowRight.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View arg0, MotionEvent arg1) {
                            switch (arg1.getAction()) {
                                case MotionEvent.ACTION_DOWN: {

                                    studentSelector++;

                                    if (studentSelector == listStudents.size())
                                        studentSelector = 0;

                                    setViewInfo(listStudents.get(studentSelector));

                                    break;
                                }
                            }
                            return true;
                        }
                    });

                    imgArrowLeft.setVisibility(View.VISIBLE);

                    imgArrowLeft.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View arg0, MotionEvent arg1) {

                            switch (arg1.getAction()) {
                                case MotionEvent.ACTION_DOWN: {

                                    studentSelector--;

                                    if (studentSelector < 0)
                                        studentSelector = listStudents.size() - 1;

                                    setViewInfo(listStudents.get(studentSelector));
                                    break;
                                }
                            }

                            return true;

                        }
                    });

                }
                else{

                    imgArrowRight.setVisibility(View.INVISIBLE);
                    imgArrowLeft.setVisibility(View.INVISIBLE);

                }

            }

            else{

                setViewInfo(null);

            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    private List<Student> getStudentInfo(Connect connect) {

        List<Student> listStudents = new ArrayList<>();

        try {
            BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(connect.getClientSocket().getOutputStream()));
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connect.getClientSocket().getInputStream()));

            JSONObject json = new JSONObject();

            json.put("code", TransmissionCodes.REQUEST_CHILD_LIST);

            outToServer.write(json.toString());
            outToServer.newLine();
            outToServer.flush();

            String incString = inFromServer.readLine();

            if (incString != null) {
                incString = incString.trim();
            }
            else {
                incString = "";
                showErrorMessage();
                connect.getClientSocket().close();

            }

            JSONObject recievedJSON = new JSONObject(incString);

            if (recievedJSON.getInt("code") == TransmissionCodes.RESPONSE_CHILD_LIST){

                for (int i = 0; i < recievedJSON.getJSONArray("array").length(); i++){

                    JSONObject tempJson = recievedJSON.getJSONArray("array").getJSONObject(i);

                    if (tempJson.getInt("rideid") != 0) {

                        Bus bus = new Bus(tempJson.getJSONObject("bus").getString("busspz"), tempJson.getJSONObject("bus").getString("photo"));

                        Assistant assistant = new Assistant(tempJson.getJSONObject("assistant").getString("name"), tempJson.getJSONObject("assistant").getString("photo"),
                                tempJson.getJSONObject("assistant").getString("contactphone"));

                        BusStop busstop = new BusStop(tempJson.getJSONObject("busstop").getInt("busstopid"), tempJson.getJSONObject("busstop").getInt("ridestopid"),
                                tempJson.getJSONObject("busstop").getString("gpsx"), tempJson.getJSONObject("busstop").getString("gpsy"), tempJson.getJSONObject("busstop").getString("name"),
                                tempJson.getJSONObject("busstop").getString("notebusstop"), tempJson.getJSONObject("busstop").getString("noteridestop"));

                        final Student student = new Student(tempJson.getString("name"), tempJson.getString("photo"), busstop, assistant, bus, tempJson.getInt("rideid"),
                                tempJson.getJSONObject("busstop").getString("ridedate"), tempJson.getBoolean("rideinprogress"),tempJson.getString("direction"));

                        listStudents.add(student);

                    }

                    else{
                        Student student = new Student(tempJson.getString("name"), tempJson.getString("photo"), null, null, null, tempJson.getInt("rideid"), "Not Available", false, "");

                        listStudents.add(student);

                    }


                }

            }

            return listStudents;


        } catch (IOException e) {
            showErrorMessage();
            try {
                connect.getClientSocket().close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listStudents;

    }

    private void setViewInfo(final Student student){

        if (student != null) {

            selectedStudent = student;

            if (student.getRideid() == 0){ //STUDENT EXISTS, BUT NO RIDES ATTACHED

                imgStudent.setImageBitmap(student.getPhotoBitmap());

                imgBus.setVisibility(View.INVISIBLE);
                imgAssistant.setVisibility(View.INVISIBLE);
                imgBusStop.setVisibility(View.INVISIBLE);

                lblNextTime.setText(getString(R.string.no_rides_planned_for_today));
                lblNextTime.setTextColor(Color.RED);

                lblBoardTimeLabel.setVisibility(View.INVISIBLE);
                lblBus.setVisibility(View.INVISIBLE);
                lblAssistant.setVisibility(View.INVISIBLE);
                lblStop.setVisibility(View.INVISIBLE);
                lblRideDateLabel.setVisibility(View.INVISIBLE);
                lblRideDate.setVisibility(View.INVISIBLE);

                lblStudentName.setText(student.getName());
                lblBoardTime.setText("");

                btnShowMap.setVisibility(View.INVISIBLE);


            }
            else{

                Timer oneTimeTimer = new Timer();
                oneTimeTimer.schedule(new ReloadTimerTask(), 100);

                imgStudent.setImageBitmap(student.getPhotoBitmap());

                imgBus.setVisibility(View.VISIBLE);
                imgBus.setImageBitmap(student.getBus().getPhotoBitmap());

                imgBus.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {

                        switch (arg1.getAction()) {
                            case MotionEvent.ACTION_DOWN: {

                                final Dialog dialog = new Dialog(RideInfoActivity.this);
                                dialog.setContentView(R.layout.dialog_bus);
                                dialog.setTitle(getString(R.string.bus));

                                ImageView imgBusPhoto = (ImageView) dialog.findViewById(R.id.imgBusDialogPhoto);
                                imgBusPhoto.setImageBitmap(student.getBus().getPhotoBitmap());

                                TextView lblBusSpz = (TextView) dialog.findViewById(R.id.txtSPZ);
                                lblBusSpz.setText(student.getBus().getSpz());

                                Button btnBusClose = (Button) dialog.findViewById(R.id.btnCloseBus);

                                btnBusClose.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();

                                break;
                            }
                        }

                        return true;

                    }
                });

                imgAssistant.setVisibility(View.VISIBLE);
                imgAssistant.setImageBitmap(student.getAssistant().getPhotoBitmap());
                imgAssistant.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {

                        switch (arg1.getAction()) {
                            case MotionEvent.ACTION_DOWN: {

                                final Dialog dialog = new Dialog(RideInfoActivity.this);
                                dialog.setContentView(R.layout.dialog_assistant_profile);
                                dialog.setTitle(getString(R.string.bus_assistant));

                                ImageView imgAssisPhoto = (ImageView) dialog.findViewById(R.id.imgAssistantPhoto);
                                imgAssisPhoto.setImageBitmap(student.getAssistant().getPhotoBitmap());

                                TextView lblAssistName = (TextView) dialog.findViewById(R.id.lblAssistantName);
                                lblAssistName.setText(student.getAssistant().getName());

                                TextView lblAssistPhone = (TextView) dialog.findViewById(R.id.lblAssistantContactPhone);
                                lblAssistPhone.setText(student.getAssistant().getPhone());

                                Button btnAssistClose = (Button) dialog.findViewById(R.id.btnAssisClose);

                                btnAssistClose.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();

                                break;
                            }
                        }

                        return true;

                    }
                });

                imgBusStop.setVisibility(View.VISIBLE);
                imgBusStop.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {

                        switch (arg1.getAction()) {
                            case MotionEvent.ACTION_DOWN: {

                                final Dialog dialog = new Dialog(RideInfoActivity.this);
                                dialog.setContentView(R.layout.dialog_busstop_details);
                                dialog.setTitle(getString(R.string.your_bus_stop_details));


                                TextView lblStopName = (TextView) dialog.findViewById(R.id.lblBusStopName);

                                lblStopName.setText(student.getStop().getName());

                                TextView lblStopNote = (TextView) dialog.findViewById(R.id.lblStopNote);
                                lblStopNote.setText(student.getStop().getNote());

                                MapView map = (MapView) dialog.findViewById(R.id.map);
                                IMapController mapController = map.getController();

                                mapController.setZoom(16);
                                GeoPoint startPoint = new GeoPoint(Float.valueOf(student.getStop().getGpsx()), Float.valueOf(student.getStop().getGpsy()));
                                mapController.setCenter(startPoint);

                                map.setBuiltInZoomControls(true);
                                map.setMultiTouchControls(true);

                                OverlayItem busStopPosition = new OverlayItem("Location", student.getStop().getName(),
                                        new GeoPoint(Float.valueOf(student.getStop().getGpsx()), Float.valueOf(student.getStop().getGpsy())));

                                Drawable busStopMarker = RideInfoActivity.this.getResources().getDrawable(R.drawable.busstop_marker2);


                                busStopPosition.setMarker(busStopMarker);

                                ArrayList<OverlayItem> overlayItemArray  = new ArrayList<OverlayItem>();

                                overlayItemArray.add(busStopPosition);

                                ItemizedIconOverlay<OverlayItem> itemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(RideInfoActivity.this, overlayItemArray, null);

                                // Add the overlay to the MapView
                                map.getOverlays().add(itemizedIconOverlay);

                                Button btnStopClose = (Button) dialog.findViewById(R.id.btnStopClose);

                                btnStopClose.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();

                                break;
                            }
                        }

                        return true;

                    }
                });


                lblNextTime.setText(getString(R.string.next_ride_details));
                lblNextTime.setTextColor(Color.GRAY);

                lblBoardTimeLabel.setVisibility(View.VISIBLE);
                lblBus.setVisibility(View.VISIBLE);
                lblAssistant.setVisibility(View.VISIBLE);
                lblStop.setVisibility(View.VISIBLE);
                lblRideDateLabel.setVisibility(View.VISIBLE);
                lblRideDate.setVisibility(View.VISIBLE);

                lblStudentName.setText(student.getName());
                lblBoardTime.setText(student.getStop().getBoardTime());
                lblRideDate.setText(student.getRidedate());

                if (student.getRidedirection().equals("To School"))
                    lblBoardTimeLabel.setText(getString(R.string.pick_up_time));
                else
                    lblBoardTimeLabel.setText(getString(R.string.drop_off_time));

                if (student.getRideinprogress())
                    btnShowMap.setVisibility(View.VISIBLE);
                else
                    btnShowMap.setVisibility(View.INVISIBLE);



            }


        }
        else{

            Intent intent = new Intent(RideInfoActivity.this, NoKidsActivity.class);

            refreshtimer.cancel();

            startActivity(intent);
            finish();
        }

    }

    private JSONObject getLatestBusCoordinates(Connect connect) {

        try {
            BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(connect.getClientSocket().getOutputStream()));
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connect.getClientSocket().getInputStream()));

            JSONObject json = new JSONObject();

            json.put("code", TransmissionCodes.REQUEST_LAST_KNOWN_GPS);
            json.put("rideid", selectedStudent.getRideid());

            outToServer.write(json.toString());
            outToServer.newLine();
            outToServer.flush();

            String incString = inFromServer.readLine();

            if (incString != null) {
                incString = incString.trim();
            }
            else {
                incString = "";
                showErrorMessage();
                connect.getClientSocket().close();

            }

            JSONObject recievedJSON = new JSONObject(incString);

            if (recievedJSON.getInt("code") == TransmissionCodes.RESPONSE_LAST_KNOWN_GPS) {

                return recievedJSON;

            }


        } catch (IOException e) {
            showErrorMessage();
            try {
                connect.getClientSocket().close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new JSONObject();


    }

    private void showErrorMessage(){

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(RideInfoActivity.this).create();
                alertDialog.setTitle(getString(R.string.failure));
                alertDialog.setMessage(getString(R.string.error_no_network));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

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
            refreshtimer = new Timer();
            refreshtimer.scheduleAtFixedRate(new ReloadTimerTask(), 500, 15000);
        }

        super.onResume();
    }

}
