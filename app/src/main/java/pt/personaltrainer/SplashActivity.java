package pt.personaltrainer;
//Class is used to display Splash screen (logo) while loading

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends Activity
{
    private static long DELAY;
    private boolean scheduled = false;
    private Timer splashTimer;
    public static Context context;


    Boolean isLogoOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        context = this;

        SettingsHelper settingsHelper = new SettingsHelper();
        isLogoOn = settingsHelper.getLogoSetting(context);
        if (isLogoOn == true)
        {DELAY = 1500;}
        else
        {DELAY = 0;}

        splashTimer = new Timer();
        splashTimer.schedule(new TimerTask()
        {
            @Override
            public void run() //Call logic after DELAY = 1500 ms. All this time logo is displayed
            {
                SplashActivity.this.finish();

               Intent intent = new Intent(SplashActivity.this, MainActivity.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               intent.setAction(getString(R.string.actionMainActivity));
               startActivity(intent);


            }
        }, DELAY);
        scheduled = true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (scheduled)
            splashTimer.cancel();
        splashTimer.purge();
    }
}