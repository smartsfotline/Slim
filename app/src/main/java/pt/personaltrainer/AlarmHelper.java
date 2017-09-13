package pt.personaltrainer;
//Class for notifications (contain APIs).
// Handles event 'reboot' after phone reboot and resets notifications.
// Because after phone reboot all notifications are cleared

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by aliaksandr.shakavets on 23-Apr-17.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class AlarmHelper extends BroadcastReceiver {
    DBHelper dbHelper;
    int hours = 0;
    int minutes = 0;
    int seconds = 0;
    String stringMinutes;


    //Method handles events after reboot (intent.ACTION_BOOT_COMPLETED)
    @Override
    public void onReceive(Context context, Intent intent) {

        SettingsHelper settingsHelper = new SettingsHelper();

        //If intent is send from system after reboot
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))  {

            intent = new Intent(context, SettingsHelper.class);
            intent.putExtra("parameter", "reboot");
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent); //Call of settings helper with "reboot" parameter

                dbHelper = new DBHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                List<Habits> alarmHabits = dbHelper.getRecordsWithNoticiations(db);
                Habits alarmHabit;
                Notification notification;
                int i = 0;

            //No active notifications exist
                if (alarmHabits.size() ==0)   {return;}

            //Message to show user after reboot
            Toast.makeText(context, R.string.notificationsReset, Toast.LENGTH_SHORT).show();


            //Reset notification for every Alarm active habit (after reboot)
               do {
                   alarmHabit = alarmHabits.get(i);
                   NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//                   builder.setContentTitle(context.getString(R.string.notificationHeader));
//                   builder.setContentText(String.valueOf(alarmHabit.name));
//                   builder.setOngoing(false);
//                   builder.setAutoCancel(true);
//                   builder.setSmallIcon(alarmHabit.photoId);
                   builder.setContentTitle(String.valueOf(alarmHabit.name)); //Title
                   builder.setContentText(context.getString(R.string.notificationHeader)); //Text
                   builder.setSmallIcon(R.mipmap.ic_launcher); //Small Icon

                   Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), alarmHabit.getPhotoID());
                   Bitmap resizedLargeIcon = Bitmap.createScaledBitmap(largeIcon , 100, 100, false);
                   builder.setLargeIcon(resizedLargeIcon); //Large icon

                   builder.setOngoing(false); //Not ongoing
                   //Ongoing notifications differ from regular notifications in the following ways:
                   //Ongoing notifications are sorted above the regular notifications in the notification panel.
                   //Ongoing notifications do not have an 'X' close button, and are not affected by the "Clear all" button.

                   builder.setAutoCancel(true); //automatically canceled when the user clicks it in the panel


                   if (settingsHelper.getSoundSetting(context) == true)
                   {builder.setDefaults(Notification.DEFAULT_SOUND);} //Sound ON if it is ON in settings
                   else
                   {builder.setDefaults(Notification.DEFAULT_LIGHTS);} //Sound OFF, only lights are ON

                    Intent mainIntent = new Intent(context, CardDetails.class); //Card details Screen is to be opened
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK); //In new window
                    mainIntent.putExtra("ID", alarmHabit.ID); //ID of habit is passed into sreen
                    mainIntent.setAction("Hello"); //Some ID, just stupid name "Hello"

                   //add intent to pending intent
                    PendingIntent pendingMainIntent = PendingIntent.getActivity(context, alarmHabit.ID, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                   //add pending intent to builder
                    builder.setContentIntent(pendingMainIntent);

                   //build notification from builder object
                    notification = builder.build();

                   //Create new intent to AlarmHelper and pass notification to this intent
                    Intent alarmIntent = new Intent(context, AlarmHelper.class);
                    alarmIntent.putExtra("notification", notification);
                    alarmIntent.putExtra("id",alarmHabit.ID);
                    alarmIntent.setAction("Hello");
                   //Add this alarmintent (with notification) to AlarmHelper to pending intent
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmHabit.ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                   //Get Alarm manager
                    AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                   //Set notification time and period with help of Calendar
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int hoursCurrent = calendar.get(Calendar.HOUR_OF_DAY);
                    int minutesCurrent = calendar.get(Calendar.MINUTE);
                    int seconds = 0;

                    calendar.set(Calendar.HOUR_OF_DAY, alarmHabit.alarmhours);
                    calendar.set(Calendar.MINUTE, alarmHabit.alarmminutes);
                    calendar.set(Calendar.SECOND, seconds);

                   if (hoursCurrent > alarmHabit.alarmhours)
                   {
//                       calendar.add(Calendar.DATE, 1);
                       calendar.add(Calendar.SECOND, SettingsHelper.notificationPeriod/1000);
                   }

                   if (hoursCurrent == alarmHabit.alarmhours)
                   {
                       if (minutesCurrent >= alarmHabit.alarmminutes)
                       {
//                           calendar.add(Calendar.DATE, 1);
                           calendar.add(Calendar.SECOND, SettingsHelper.notificationPeriod/1000);
                       }
                   }
                  //pass into Notification Manager (Alarm Manager) type, time of notification, period, and intent
                   manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), SettingsHelper.notificationPeriod, pendingIntent);
                   i = i + 1;
                }
                while ( i < alarmHabits.size());
                db.close();
                Toast.makeText(context, R.string.notificationsReset, Toast.LENGTH_SHORT).show();

        }

        //Process notifications received from Android Alarm Manager (sent there from my logic)
        else
        {
            //Receive intent and notification in it (From system Alarm/Notification manager as Broadcast receiver)
            //Immediately call notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = intent.getParcelableExtra("notification");
            int id = intent.getIntExtra("id", 1);
            notificationManager.notify(id, notification); //Call of notification (
        }
    }

// API to update all the active notifications (it is called after changes in Settings)
  public void updateAllActiveNotifications(Context context){
      SettingsHelper settingsHelper = new SettingsHelper();
      if (!settingsHelper.getNotificationSetting(context) == false)  {return;}

      //Receive all the active notifications
      dbHelper = new DBHelper(context);
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      List<Habits> alarmHabits = dbHelper.getRecordsWithNoticiations(db);
      Habits alarmHabit;
      Notification notification;
      int i = 0;

      //return if no active notifications
      if (alarmHabits.size() == 0)    {return;}

      do {
          //Update notifications with the up-to-data parameters
          alarmHabit = alarmHabits.get(i);
          NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
          builder.setAutoCancel(false);

          builder.setContentTitle(String.valueOf(alarmHabit.name));
          builder.setContentText(context.getString(R.string.notificationHeader));

          builder.setSmallIcon(R.mipmap.ic_launcher);
          Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), alarmHabit.getPhotoID());
          Bitmap resizedLargeIcon = Bitmap.createScaledBitmap(largeIcon , 100, 100, false);
          builder.setLargeIcon(resizedLargeIcon);

         if (settingsHelper.getSoundSetting(context) == true) //Sound settings
          {builder.setDefaults(Notification.DEFAULT_SOUND); }
         else
          {builder.setDefaults(Notification.DEFAULT_LIGHTS);}

          Intent mainIntent = new Intent(context, CardDetails.class);
          mainIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
          mainIntent.putExtra("ID", alarmHabit.ID);
          mainIntent.setAction("Hello");
          PendingIntent pendingMainIntent = PendingIntent.getActivity(context, alarmHabit.ID, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);
          builder.setContentIntent(pendingMainIntent);

          builder.setOngoing(false);
          builder.setAutoCancel(true);

          notification = builder.build();
          Intent alarmIntent = new Intent(context, AlarmHelper.class);
          alarmIntent.putExtra("notification", notification);
          alarmIntent.putExtra("id",alarmHabit.ID);
          alarmIntent.setAction("Hello");

          PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmHabit.ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
          AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

          Calendar calendar = Calendar.getInstance();
          calendar.setTimeInMillis(System.currentTimeMillis());
          int hoursCurrent = calendar.get(Calendar.HOUR_OF_DAY);
          int minutesCurrent = calendar.get(Calendar.MINUTE);
          int seconds = 0;

          calendar.set(Calendar.HOUR_OF_DAY, alarmHabit.alarmhours);
          calendar.set(Calendar.MINUTE, alarmHabit.alarmminutes);
          calendar.set(Calendar.SECOND, seconds);

          if (alarmHabit.alarmhours < hoursCurrent)
          {
//              calendar.add(Calendar.DATE, 1);
              calendar.add(Calendar.SECOND, SettingsHelper.notificationPeriod/1000);
          }

          if (alarmHabit.alarmhours == hoursCurrent)
          {
              if (alarmHabit.alarmminutes <= minutesCurrent)
              {
//                  calendar.add(Calendar.DATE, 1);
                  calendar.add(Calendar.SECOND, SettingsHelper.notificationPeriod/1000);
              }
          }

          manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), SettingsHelper.notificationPeriod, pendingIntent);
          i = i + 1;
      }
      while ( i < alarmHabits.size());
      db.close();

  }

//Cancel all the notifications (called from the Settings, if notifications are prohibited)
  public void cancelAllNotifications(Context context){
      dbHelper = new DBHelper(context);
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      List<Habits> alarmHabits = dbHelper.getData(db);
      Habits alarmHabit;

      if (alarmHabits.size() == 0)
      {return;}

      int i = 0;
      do {
          alarmHabit = alarmHabits.get(i);
          dbHelper.updateHabitAlarmStatus(db, alarmHabit.ID, SettingsHelper.alarmStatusNotActive);
          // dbHelper.close();

          Intent alarmIntent = new Intent(context, AlarmHelper.class);
          alarmIntent.setAction("Hello");
          PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmHabit.ID, alarmIntent, 0);
          AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
          manager.cancel(pendingIntent);
          i = i+1;
      }
      while ( i < alarmHabits.size());

      Toast.makeText(context, R.string.notificationsDeactivated, Toast.LENGTH_SHORT).show();

  }

  //Cancel some particular notificaion (if user switched it off, or aim is reached)
    public void cancelNotification(Context context, int id){
        dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
           Habits alarmHabit = dbHelper.getRecord(db, id);

             dbHelper.close();

            Intent alarmIntent = new Intent(context, AlarmHelper.class);
            alarmIntent.setAction("Hello");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmHabit.ID, alarmIntent, 0);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.cancel(pendingIntent);
    }
    }


