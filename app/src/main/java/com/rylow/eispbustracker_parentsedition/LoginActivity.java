package com.rylow.eispbustracker_parentsedition;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.rylow.eispbustracker_parentsedition.network.Connect;
import com.rylow.eispbustracker_parentsedition.network.ConnecterAsyncTask;
import com.rylow.eispbustracker_parentsedition.network.TransmissionCodes;
import com.rylow.eispbustracker_parentsedition.service.RideInfoIntentService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by s.bakhti on 30.3.2016.
 */
public class LoginActivity extends AppCompatActivity implements Serializable {



    private String username, password;
    public static Boolean serviceON = false;

    private final Context mContext = this;
    private final String SENDER_ID = "647473183411"; // Project Number at https://console.developers.google.com/project/...
    private final String SHARD_PREF = "busTrackerSettings";
    private final String GCM_TOKEN = "gcmtoken";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //if (!serviceON)
        //    startService(new Intent(this, RideInfoIntentService.class));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText textUsername = (EditText) findViewById(R.id.inputUsername);
        final EditText textPassword = (EditText) findViewById(R.id.inputPassword);
        final CheckBox cboxSave = (CheckBox) findViewById(R.id.cboxSave);

        final SharedPreferences settings = getSharedPreferences(SHARD_PREF, MODE_PRIVATE);

        username = settings.getString("username", "");
        password = settings.getString("password", "");

        textUsername.setText(username);
        textPassword.setText(password);

        if (username.length() > 0)
            cboxSave.setChecked(true);

        String token = settings.getString(GCM_TOKEN, "");
        if (token.isEmpty()) {
            try {
                getGCMToken();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final ImageView imageLogin = (ImageView) findViewById(R.id.imageViewLogin);
        imageLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        username = textUsername.getText().toString();
                        password = textPassword.getText().toString();

                        if (cboxSave.isChecked()){
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.commit();
                        }
                        else{

                            SharedPreferences.Editor editor = settings.edit();

                            editor.putString("username", "");
                            editor.putString("password", "");
                            editor.commit();
                        }


                        Connect connect = Connect.getInstance();
                        connect.setPassword(password);
                        connect.setUsername(username);

                        AsyncTask query = new AsyncTask<Integer, Void, Boolean>(){

                            @Override
                            protected Boolean doInBackground(Integer... params) {

                                Connect connect = Connect.getInstance();

                                if (connect.connect()){



                                    try {
                                        JSONObject json = new JSONObject();

                                        Connect conn = Connect.getInstance();

                                        BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(conn.getClientSocket().getOutputStream()));

                                        json.put("code", TransmissionCodes.GCM_CODE);
                                        json.put("gcm", settings.getString(GCM_TOKEN, ""));

                                        String send = json.toString();

                                        outToServer.write(send);
                                        outToServer.newLine();
                                        outToServer.flush();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                    Intent intent = new Intent(LoginActivity.this, RideInfoActivity.class);

                                    startActivity(intent);
                                    finish();

                                }
                                else {

                                    if (connect.connect()){

                                        try {
                                            JSONObject json = new JSONObject();

                                            Connect conn = Connect.getInstance();

                                            BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(conn.getClientSocket().getOutputStream()));

                                            json.put("code", TransmissionCodes.GCM_CODE);
                                            json.put("gcm", settings.getString(GCM_TOKEN, ""));

                                            String send = json.toString();

                                            outToServer.write(send);
                                            outToServer.newLine();
                                            outToServer.flush();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                        Intent intent = new Intent(LoginActivity.this, RideInfoActivity.class);

                                        startActivity(intent);
                                        finish();

                                    }
                                    else {

                                        final Context context = getApplicationContext();
                                        final CharSequence message = getString(R.string.login_failed);
                                        final int duration = Toast.LENGTH_SHORT;

                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                Toast toast = Toast.makeText(context, message, duration);
                                                toast.show();
                                            }
                                        });
                                    }


                                }

                                return true;
                            }
                        }.execute();


                    }
                }
                return true;
            }
        });


    }

    private void getGCMToken() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InstanceID instanceID = InstanceID.getInstance(mContext);
                    String token = instanceID.getToken(SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    if (token != null && !token.isEmpty()) {
                        SharedPreferences appPrefs = mContext.getSharedPreferences(SHARD_PREF, Context.MODE_PRIVATE);
                        SharedPreferences.Editor prefsEditor = appPrefs.edit();
                        prefsEditor.putString(GCM_TOKEN, token);
                        prefsEditor.apply();
                    }
                    Log.i("GCM", token);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }




}
