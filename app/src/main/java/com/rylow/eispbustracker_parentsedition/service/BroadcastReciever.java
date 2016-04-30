package com.rylow.eispbustracker_parentsedition.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by s.bakhti on 30.4.2016.
 */
public class BroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, RideInfoIntentService.class);
        context.startService(startServiceIntent);
    }
}
