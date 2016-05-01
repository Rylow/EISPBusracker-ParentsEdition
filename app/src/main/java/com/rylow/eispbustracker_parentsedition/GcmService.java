package com.rylow.eispbustracker_parentsedition;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * Created by bakht on 01.05.2016.
 */
public class GcmService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {


        JSONObject jsonObject = new JSONObject();
        Set<String> keys = data.keySet();
        for (String key : keys) {
            try {
                jsonObject.put(key, data.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            sendNotification(jsonObject.toString(5));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeletedMessages() {
        sendNotification("Deleted messages on server");
    }

    @Override
    public void onMessageSent(String msgId) {
        sendNotification("Upstream message sent. Id=" + msgId);
    }

    @Override
    public void onSendError(String msgId, String error) {
        sendNotification("Upstream message send error. Id=" + msgId + ", error" + error);
    }

    private void sendNotification(final String msg) {

        JSONObject json = null;

        try {
           json = new JSONObject(msg);


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(GcmService.this)
                                .setSmallIcon(R.mipmap.icon)
                                .setContentTitle(getString(R.string.notification_header))
                                .setContentText(json.getString("message"));
                // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(GcmService.this, LoginActivity.class);


                TaskStackBuilder stackBuilder = TaskStackBuilder.create(GcmService.this);

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

                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
