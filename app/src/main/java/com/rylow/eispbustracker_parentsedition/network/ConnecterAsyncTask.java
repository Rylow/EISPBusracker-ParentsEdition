package com.rylow.eispbustracker_parentsedition.network;

import android.os.AsyncTask;

/**
 * Created by s.bakhti on 31.3.2016.
 */
public class ConnecterAsyncTask extends AsyncTask<Integer, Void, Boolean> {

    Connect connect = Connect.getInstance();

    @Override
    protected Boolean doInBackground(Integer... params) {



        if (connect.getClientSocket().isConnected()){

            return connect.auth();

        }
        else{
            return connect.connect();
        }
    }
}
