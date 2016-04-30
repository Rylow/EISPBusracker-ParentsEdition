package com.rylow.eispbustracker_parentsedition.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.rylow.eispbustracker_parentsedition.LoginActivity;
import com.rylow.eispbustracker_parentsedition.R;
import com.rylow.eispbustracker_parentsedition.network.Connect;

import java.io.IOException;

/**
 * Created by s.bakhti on 19.4.2016.
 */
public class NoKidsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_no_children);

        Button btnClose = (Button) findViewById(R.id.btnBackToLogin);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(NoKidsActivity.this, LoginActivity.class);

                try {
                    Connect.getInstance().getClientSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startActivity(intent);
                finish();

            }
        });


    }





}
