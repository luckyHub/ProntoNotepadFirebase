package com.okason.prontonotepad.ui.reminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.okason.prontonotepad.R;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.ui.addNote.AddNoteActivity;
import com.okason.prontonotepad.util.Constants;

public class ReminderActivity extends AppCompatActivity {
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    private Activity mActivity;
    private Note mCurrentNote;
    private String serializedNote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActivity = this;


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setAlarm(){
        alarmManager = (AlarmManager)mActivity.getSystemService(Context.ALARM_SERVICE);

    }

    private void buildNotification(){
        String message =  "Delete " + mCurrentNote.getContent().substring(0, Math.min(mCurrentNote.getContent().length(), 50)) + "  ... ?";
        NotificationCompat.Builder notificationBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(mActivity)
                .setSmallIcon(R.drawable.com_facebook_button_icon)
                .setContentTitle(mCurrentNote.getTitle())
                .setContentText(message);

        Intent resultIntent = new Intent(mActivity, AddNoteActivity.class);
        if (!TextUtils.isEmpty(serializedNote)){
            resultIntent.putExtra(Constants.SERIALIZED_NOTE, serializedNote);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mActivity);
        stackBuilder.addParentStack(AddNoteActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(12, notificationBuilder.build());



    }

}
