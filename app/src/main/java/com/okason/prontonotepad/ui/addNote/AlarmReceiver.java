package com.okason.prontonotepad.ui.addNote;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.util.Constants;

/**
 * Created by Valentine on 1/25/2017.
 */

public class AlarmReceiver extends BroadcastReceiver{
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent !=null && intent.hasExtra(Constants.SERIALIZED_NOTE)){
            String serializedNote = intent.getStringExtra(Constants.SERIALIZED_NOTE);
            if (!TextUtils.isEmpty(serializedNote)){
                buildNotification(serializedNote, context);
            }
        }
    }


    private void buildNotification(String serializedNote, Context context){

        Gson gson = new Gson();
        Note passedInNote = gson.fromJson(serializedNote, Note.class);


        String message =  passedInNote.getContent().substring(0, Math.min(passedInNote.getContent().length(), 50));
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(passedInNote.getTitle())
                        .setSound(alarmSound)
                        .setContentText(message);

        Intent resultIntent = new Intent(context, AddNoteActivity.class);
        if (!TextUtils.isEmpty(serializedNote)){
            resultIntent.putExtra(Constants.SERIALIZED_NOTE, serializedNote);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(AddNoteActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(12, notificationBuilder.build());



    }

}
