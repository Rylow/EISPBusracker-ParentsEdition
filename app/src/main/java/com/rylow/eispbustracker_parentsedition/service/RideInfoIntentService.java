package com.rylow.eispbustracker_parentsedition.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.support.v4.app.NotificationCompat;
import android.view.View;

import com.rylow.eispbustracker_parentsedition.LoginActivity;
import com.rylow.eispbustracker_parentsedition.R;
import com.rylow.eispbustracker_parentsedition.network.Connect;
import com.rylow.eispbustracker_parentsedition.network.TransmissionCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by s.bakhti on 30.4.2016.
 */
public class RideInfoIntentService extends IntentService {


    public RideInfoIntentService() {
        super("Intent Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final SharedPreferences settings = getSharedPreferences("busTrackerSettings", MODE_PRIVATE);

        Connect conn = Connect.getInstance();
        conn.setUsername(settings.getString("username", "null"));
        conn.setPassword(settings.getString("password", "null"));


        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new ReloadTimerTask(), 30000, 30000);



    }

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

                        requestStatus(outToServer, inFromServer, connect);

                    } else {


                    }
                } else {

                    BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(connect.getClientSocket().getOutputStream()));
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connect.getClientSocket().getInputStream()));

                    requestStatus(outToServer, inFromServer, connect);
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

    private void requestStatus(BufferedWriter outToServer, BufferedReader inFromServer, Connect connect) throws IOException, JSONException {

        JSONObject json = new JSONObject();



        json.put("code", TransmissionCodes.REQUEST_RIDE_STATUS_ALL_ATTACHED_KIDS);
        json.put("username", connect.getUsername());

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

        if (recievedJSON.getInt("code") == TransmissionCodes.RESPONSE_RIDE_STATUS_ALL_ATTACHED_KIDS) {

            JSONArray array = recievedJSON.getJSONArray("array");

            for (int i = 0; i < array.length(); i++){

                JSONObject child = array.getJSONObject(i);

                if (child.getBoolean("rideinprogress")){


                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(this)
                                        .setSmallIcon(R.mipmap.icon)
                                        .setContentTitle("Bus is in transit")
                                        .setContentText(child.getString("name") + "'s bus is now in transit!");
                        // Creates an explicit intent for an Activity in your app
                        Intent resultIntent = new Intent(this, LoginActivity.class);


                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

                        stackBuilder.addParentStack(LoginActivity.class);

                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);
                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        mNotificationManager.notify(0, mBuilder.build());
                    }




                }


            }


        }

    }

}
