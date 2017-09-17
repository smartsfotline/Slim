package pt.personaltrainer;
//Class with: 1.constants and settings 2.Methods supplying 'Settings' page in the app
//Class is launched first after user clicks the icon to initialyze constants

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by aliaksandr.shakavets on 29-Apr-17.
 */


public class SettingsHelper extends Activity {

    static public Context context;
    //Settings constants
    static final int SETTING_NOTIFICATONS = 1;
    static final int SETTING_SOUND = 2;
    static final int SETTING_LOGO = 3;
    static final int SETTING_HOURS = 4;

    //Status constants
    public static final int statusCompletedGold    = -2;
    public static final int statusCompletedSilver  = -1;
    public static final int statusNew              = 0;
    public static final int statusPaused           = 1;
    public static final int statusActiveResumed    = 2;
    public static final int statusActiveNonStop    = 3;

   //Alarm status constants
    public static final int alarmStatusNotActive = 0;
    public static final int alarmStatusActive = 1;

    //Stars statsus constants
    public static final int noStar = 0;
    public static final int silverStar = 2;
    public static final int goldStar = 1;

    //MAIN Settings constants
    public static final int maxHabitsNumber = 3;
    public static final long aimTime = 1000*60*60*24*21; //21 days
    public static final int notificationPeriod = 1000*60*60*1; //1 hour

    //time unit constants
    public static final long timeRateinSeconds = 1000;
    public static final long timeRateinMinutes = 1000*60;
    public static final long timeRateinHours = 1000*60*60;
    public static final long timeRateinDays = 1000*60*60*24;


    public static Boolean isFirstCall = false;
    public static long timeRate = timeRateinDays; //default time rate
    public static final int sleepTime = 10; //Pause time (miliseconds)


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;

        //On loading of Settings page check calling intent and passed parameters
        Intent onReceiver = getIntent();
        String parameter = "parameter";
        String logoparameter = "logoparameter";

        String value = onReceiver.getStringExtra(parameter);
        String logovalue = onReceiver.getStringExtra(logoparameter);

        if (value == null) {value = "null";}
        if (logovalue == null) {logovalue = "null";}


        if ( value.equals("reboot") )
        {
            finish(); //finish if parameter 'parameter' value = 'reboot' (NO need to start UI, just update notifications)
        }
        else { //if no reboot parameter than Start MainActiviy or SplashActivity (logo)

            if ( (getLogoSetting(this) == true) && (!logovalue.equals("nologo")) )  {
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);  //Call Logo (Splash Activity) if setting is NOT "nologo"
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(getString(R.string.actionMainActivity));
                startActivity(intent);  //Call Main Activity if setting = "nologo"
            }

            //Make micropause
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finish();
        }
    }

    //Setters API
    public void setNotificationSetting (Boolean value, Context context)
    {
    //this.notificationSetting = value;
        setMySetting(SETTING_NOTIFICATONS, value, context);
    }

    public void setSoundSetting (Boolean value,  Context context)
    {
    // this.soundSetting = value;
        setMySetting(SETTING_SOUND, value, context);
    }

    public void setLogoSetting (Boolean value,  Context context)
    {
    // this.logoSetting = value;
        setMySetting(SETTING_LOGO, value, context);
    }

    public void setHoursSetting (Boolean value, Context context)
    {
        //  this.hoursSetting = value;
        setMySetting(SETTING_HOURS, value, context);
    }

    //Getters API
    public Boolean getNotificationSetting (Context context){
        Boolean value;
        value = getMySetting(SETTING_NOTIFICATONS, context);
        return value;
    }

    public Boolean getSoundSetting (Context context){
        Boolean value;
        value = getMySetting(SETTING_SOUND, context);
        return value;
    }


    public Boolean getLogoSetting (Context context){
        Boolean value;
        value = getMySetting(SETTING_LOGO, context);
        return value;
    }

    public Boolean getHoursSetting (Context context){
        Boolean value;
        value = getMySetting(SETTING_HOURS, context);
        return value;
    }

    //Main method updating Setting records in DB
    public void setMySetting (int settingID, Boolean settingValue,  Context context){
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int settingValueInt = 3;
        if (settingValue == true) {settingValueInt = 1;}
        else {settingValueInt = 0;}
        dbHelper.setSetting(db, settingID, settingValueInt);
        db.close();
      }

    //Main method reading Setting records in DB
    public Boolean getMySetting (int settingID,  Context context){
        Boolean parameter = false;
        int settingValue = 3;
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        settingValue = dbHelper.getSetting(db, settingID);

        if (settingValue == 1) {parameter = true;}
        else if (settingValue == 0) {parameter = false;}
        return parameter;
    }
}
