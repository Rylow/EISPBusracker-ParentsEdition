package com.rylow.eispbustracker_parentsedition;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.rylow.eispbustracker_parentsedition.network.Connect;
import com.rylow.eispbustracker_parentsedition.network.ConnecterAsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by s.bakhti on 30.3.2016.
 */
public class LoginActivity extends AppCompatActivity implements Serializable {



    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText textUsername = (EditText) findViewById(R.id.inputUsername);
        final EditText textPassword = (EditText) findViewById(R.id.inputPassword);
        final CheckBox cboxSave = (CheckBox) findViewById(R.id.cboxSave);

        final SharedPreferences settings = getSharedPreferences("busTrackerSettings", MODE_PRIVATE);

        username = settings.getString("username", "");
        password = settings.getString("password", "");

        textUsername.setText(username);
        textPassword.setText(password);

        if (username.length() > 0)
            cboxSave.setChecked(true);

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

                                    Intent intent = new Intent(LoginActivity.this, RideInfoActivity.class);

                                    startActivity(intent);
                                    finish();

                                }
                                else {

                                    final Context context = getApplicationContext();
                                    final CharSequence message = "Login Failed. Either your password is incorrect or server is not available";
                                    final int duration = Toast.LENGTH_SHORT;

                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            Toast toast = Toast.makeText(context, message, duration);
                                            toast.show();
                                        }
                                    });


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




    }
