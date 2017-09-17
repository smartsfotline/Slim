package pt.personaltrainer;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.LineHeightSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.bluejamesbond.text.DocumentView;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by aliaksandr.shakavets on 19-Apr-17.
 */

public class CardDetails extends AppCompatActivity{

    //Класс, для структурирования текста
    public static class MySpan implements LineHeightSpan {
        public final int height;

       public MySpan(int height) {
            this.height = height;
        }

        @Override
        public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v,
                                 Paint.FontMetricsInt fm) {
            fm.ascent -= height;
            fm.descent += height;
            fm.top += 0;
            fm.bottom += 0;
            fm.leading += 0;
        }
    }

    //Служебные атрибуты класса
    private static Context context;
    public int hoursCurrent;
    public int minutesCurrent;
    public Habits habit;

    //Вьюхи для работы с соответствующими элементами экрана
    TextView tvHabit;
    TextView tvStar;
    TextView tvState;
    TextView tvRunningText;

    TextView tvDaysLeft;
    TextView tvLableDaysLeft;
    TextView tvDaysHabitWordDays;
    TextView tvDaysLeftWordDays;

    ImageView ivStarLeft;
    ImageView ivStarBig;

    TextView tvLableHabitDays;
    TextView tvLable;

    ImageView ivHabit;
    DocumentView docView;

    CheckBox chbActive;
    CheckBox chbAlarm;
    TimePicker timePicker;
    Button restartButton;
    Button timePickerConfirmButton;
    Button timePickerCancelButton;
    RelativeLayout relativeLayout;
    RelativeLayout rlTextBlock;
    RelativeLayout.LayoutParams layoutParams;


    long currentTime;
    int ID = 0;
    int activeHabitsNumber = 0;
    int starAchieved = SettingsHelper.noStar;

    String habitName;
    String stringMinutes;
    String unitsToSet;

    DBHelper dbHelper;
    SQLiteDatabase db;
    SettingsHelper settingsHelper;
    Calendar calendar ;
    Handler handler;

   @Override
    protected void onCreate(Bundle savedInstanceState){
       super.onCreate(savedInstanceState);
       setContentView(R.layout.card_details);

       //При загрузке страницы просиходит пауза 0.2 сек (thread.sleep) для того, чтобы дать успеть заполниться всем текстовым полям
       //Через 0.2 секунды элементы синхронно становятся видимыми
       rlTextBlock = (RelativeLayout) findViewById(R.id.rlTextBlock);
       handler = new Handler() {
           public void handleMessage(android.os.Message msg)
           {
               if (msg.what==200) {  rlTextBlock.setVisibility(View.VISIBLE); }

           };
       };


       //Инициализация хелперов
       context = this;
       dbHelper = new DBHelper(this);
       db = dbHelper.getWritableDatabase();
       settingsHelper = new SettingsHelper();

       //Инициализация данных
       //Считывает данные из интента, с помощью которого была вызваа данная активити
       Intent intent = getIntent();
       if (intent == null) {finish();}
       ID = intent.getIntExtra("ID",1);

       //Инициализация текущего времени
       calendar = Calendar.getInstance();
       calendar.setTimeInMillis(System.currentTimeMillis());
       hoursCurrent = calendar.get(Calendar.HOUR_OF_DAY);
       minutesCurrent = calendar.get(Calendar.MINUTE);
       currentTime = System.currentTimeMillis();

       //С помощью ID, переданного с интентом считываем данные вызванной привычки
       this.habit = dbHelper.getRecord(db, ID);
       habitName = habit.getName();

       //Инициализация элементов UI
       //Имя привычки
       tvHabit = (TextView) findViewById(R.id.habit_name_l) ;
       tvHabit.setText(habit.getName());

       //Краткое описание привычки
       tvHabit = (TextView) findViewById(R.id.habit_description_l) ;
       tvHabit.setText(habit.getDescription());

       //Картинка привычки
       ivHabit = (ImageView) findViewById(R.id.habit_photo_l) ;
       ivHabit.setImageResource(habit.getPhotoID());

       //Звездочка (отображается только если получена звезда для привычки)
       ivStarBig = (ImageView) findViewById(R.id.habit_star_big) ;
       ivStarBig.setVisibility(View.GONE);

       //Инициализация переменных для формирования списков
       //Объявляем переменные
       SpannableStringBuilder totalSpan = new SpannableStringBuilder();
       MyBullet bulletSpan;
       Spannable span;

       //Формируем массив данных из атрибута (строка) "obligatory" привычки
       String[] partsObligatory = habit.getObligatory().split("\n");
       totalSpan = new SpannableStringBuilder();

       //Формируем список из массива данных созданных из "obligatory"
       for (String string: partsObligatory)
       {
           bulletSpan = new MyBullet(0,getResources().getColor(R.color.pickerblue));
           span = new SpannableString(string);
           span.setSpan(new MySpan(15), 0, string.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
           span.setSpan(bulletSpan, 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
           totalSpan.append(span);
           totalSpan.append("\n");
          }

  //  totalSpan.setSpan(new JustifiedSpan(), 0, totalSpan.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

      //Связываем полученный список и соответствующую вьюху на экране
       docView = (DocumentView) findViewById(R.id.habit_detailed_description_1) ;
       docView.setText(totalSpan);

      //Формируем массив данных из атрибута (строка) "optional" привычки
       String[] partsOptional = habit.getOptional().split("\n");
       totalSpan = new SpannableStringBuilder();

       //Формируем список из массива данных созданных из "optional"
       for (String string: partsOptional)
       {
           bulletSpan = new MyBullet(0,getResources().getColor(R.color.pickerblue));
           span = new SpannableString(string);
           span.setSpan(new MySpan(15), 0, string.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
           span.setSpan(bulletSpan, 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
           totalSpan.append(span);
           totalSpan.append("\n");
       }
//       totalSpan.setSpan(new JustifiedSpan(), 0, totalSpan.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

       //Связываем полученный список и соответствующую вьюху на экране
       docView = (DocumentView) findViewById(R.id.habit_detailed_description_2) ;
       docView.setText(totalSpan);

       //Формируем массив данных из атрибута (строка) "knowledge" привычки
       String[] partsKnowledge = habit.getKnowledge().split("\n");
       totalSpan = new SpannableStringBuilder();

       //Формируем список из массива данных созданных из "knowledge"
       for (String string: partsKnowledge)
       {
           bulletSpan = new MyBullet(0,getResources().getColor(R.color.pickerblue));
           span = new SpannableString(string);
           span.setSpan(new MySpan(15), 0, string.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
           span.setSpan(bulletSpan, 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
           totalSpan.append(span);
           totalSpan.append("\n");
       }
//       totalSpan.setSpan(new JustifiedSpan(), 0, totalSpan.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

       //Связываем полученный список и соответствующую вьюху на экране
       docView = (DocumentView) findViewById(R.id.habit_detailed_description_3) ;
       docView.setText(totalSpan);

     //Пауза 0.2 секунды
       Thread t = new Thread(new Runnable()
       {
           public void run()
           {
               try
               {
                   Thread.sleep(200);
                   handler.sendEmptyMessage(200);
               }
               catch (InterruptedException e)
               {
                   e.printStackTrace();
               }
           }
       }
       );
       t.start();

       //Инициализация таймера
       timePicker = (TimePicker) findViewById(R.id.timePicker1);
       timePicker.setIs24HourView(true);

       //Инициализация чекбоксов
       chbActive =                 (CheckBox)   findViewById(R.id.checkbox_activ);
       chbAlarm =                  (CheckBox)   findViewById(R.id.checkbox_alarm);

       //Инициализация кнопок для таймпикера
       timePickerConfirmButton =   (Button)     findViewById(R.id.timePickerConfirm);
       timePickerCancelButton =    (Button)     findViewById(R.id.timePickerCancel);

       //Инициализация бегущей строки
       tvRunningText =             (TextView)   findViewById(R.id.tvRuningText);

       //Инициализация вьюхи для отображения дней и текстового описания (день, дней, дня)
       tvDaysHabitWordDays =       (TextView)   findViewById(R.id.days1);
       tvLableHabitDays =          (TextView)   findViewById(R.id.lable_habit_days);

       //Инициализация вьюх для отображения статуса (play, stop, resumed) и звезд (нет, серебряная, золотая)
       tvState =                   (TextView)   findViewById(R.id.habit_state);
       tvStar =                    (TextView)   findViewById(R.id.habit_star);

      //Инициализация вьюх для отображения оставшихся до цели дней
       tvDaysLeft =                (TextView)   findViewById(R.id.days_left);
       tvLableDaysLeft =           (TextView)   findViewById(R.id.lable_days_left);
       ivStarLeft =                (ImageView)  findViewById(R.id.star_left);
       tvDaysLeftWordDays =        (TextView)   findViewById(R.id.days2);


 //Установка бегущей строки с мотивацией
//       Random randomGenerator = new Random();
//       int randIndex = randomGenerator.nextInt(25);
//       String motivation = motivation = getResources().getString(R.string.motivation_00) ;
//
//       switch(randIndex)
//       {
//           case 0:
//                motivation = getResources().getString(R.string.motivation_00);
//                break;
//           case 1:
//               motivation = getResources().getString(R.string.motivation_01);
//                break;
//           case 2:
//               motivation = getResources().getString(R.string.motivation_02);
//                break;
//           case 3:
//               motivation = getResources().getString(R.string.motivation_03);
//                break;
//           case 4:
//               motivation = getResources().getString(R.string.motivation_04);
//                break;
//           case 5:
//               motivation = getResources().getString(R.string.motivation_05);
//               break;
//           case 6:
//               motivation = getResources().getString(R.string.motivation_06);
//               break;
//           case 7:
//               motivation = getResources().getString(R.string.motivation_07);
//               break;
//           case 8:
//               motivation = getResources().getString(R.string.motivation_08);
//               break;
//           case 9:
//               motivation = getResources().getString(R.string.motivation_09);
//               break;
//           case 10:
//               motivation = getResources().getString(R.string.motivation_10);
//               break;
//           case 11:
//               motivation = getResources().getString(R.string.motivation_11);
//               break;
//           case 12:
//               motivation = getResources().getString(R.string.motivation_12);
//               break;
//           case 13:
//               motivation = getResources().getString(R.string.motivation_13);
//               break;
//           case 14:
//               motivation = getResources().getString(R.string.motivation_14);
//               break;
//           case 15:
//               motivation = getResources().getString(R.string.motivation_15);
//               break;
//           case 16:
//               motivation = getResources().getString(R.string.motivation_16);
//               break;
//           case 17:
//               motivation = getResources().getString(R.string.motivation_17);
//               break;
//           case 18:
//               motivation = getResources().getString(R.string.motivation_18);
//               break;
//           case 19:
//               motivation = getResources().getString(R.string.motivation_19);
//               break;
//           case 20:
//               motivation = getResources().getString(R.string.motivation_20);
//               break;
//           case 21:
//               motivation = getResources().getString(R.string.motivation_21);
//               break;
//           case 22:
//               motivation = getResources().getString(R.string.motivation_22);
//               break;
//           case 23:
//               motivation = getResources().getString(R.string.motivation_23);
//               break;
//           case 24:
//               motivation = getResources().getString(R.string.motivation_24);
//               break;
//
//       }
//
//       tvRunningText = (TextView) findViewById(R.id.tvRuningText);
//       tvRunningText.setText(motivation);
//       tvRunningText.setSelected(true);




//    ****************************************************************
//    *************** BUTTONS' listeners initialization *****************

       //Toolbar filling and Backspase navigation setting
       Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
       setSupportActionBar(toolbar);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       getSupportActionBar().setDisplayShowHomeEnabled(true); //Формуруем обратнус стрелку на тулбаре
       toolbar.setNavigationOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               Intent parentIntent = getIntent();
               String actionName   = parentIntent.getAction();

               if (actionName.equals(getString(R.string.goToCardAction))) //Привычка была вызвана с экрана привычек, и после этого других интентов не формировалась
               {
                   finish();  //Выйти из обработчика, но не из активити
               }
               else //Нажата кнопка Backspace. Выходим на главный экран
               {
                   Intent intentToMainActivity = new Intent(getApplicationContext(), SettingsHelper.class);
                   intentToMainActivity.setAction(getString(R.string.actionMainActivity));
                   intentToMainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   intentToMainActivity.putExtra("logoparameter", "nologo");
                   startActivity(intentToMainActivity);
               }
           }
       });

       //Устанавливаем обработчики на изменение времени в таймере. Делаем кнопки видимыми
       timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
           public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
               timePickerConfirmButton.setEnabled(true);
               timePickerConfirmButton.setVisibility(View.VISIBLE);
               timePickerCancelButton.setEnabled(true);
               timePickerCancelButton.setVisibility(View.VISIBLE);
               view.setVisibility(View.VISIBLE);
           }
       });

        //Устанавливаем обработчик кнопки "Confirm"
       timePickerConfirmButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               int hoursInPicker   = timePicker.getCurrentHour();
               int minutesInPicker = timePicker.getCurrentMinute();

               //Актуализируются данные привычки
               dbHelper.updateHabitAlarmStatus(db, ID, SettingsHelper.alarmStatusActive);
               dbHelper.updateAlarmTime(db, ID, hoursInPicker, minutesInPicker);
               habit = dbHelper.getRecord(db, ID);

               timePickerConfirmButton.setEnabled(false);
               timePickerCancelButton.setEnabled(false);

               timePickerConfirmButton.setVisibility(View.INVISIBLE);
               timePickerCancelButton.setVisibility(View.INVISIBLE);

               raiseNotification();
           }
       });

       //Устанавливаем обработчик кнопки "Cancel"
       timePickerCancelButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               timePickerConfirmButton = (Button) findViewById(R.id.timePickerConfirm);
               timePickerCancelButton  = (Button) findViewById(R.id.timePickerCancel);

               //В зависимости от статуса привычки выставляем время в таймере: или время из БД, или текущее время
               if (habit.alarmstatus == SettingsHelper.alarmStatusActive) {
                   timePicker.setCurrentHour(habit.alarmhours);
                   timePicker.setCurrentMinute(habit.alarmminutes);
               }
               else
               {
                   timePicker.setCurrentHour(hoursCurrent);
                   timePicker.setCurrentMinute(minutesCurrent);
               }
               timePickerConfirmButton.setEnabled(false);
               timePickerCancelButton.setEnabled(false);
               timePickerConfirmButton.setVisibility(View.INVISIBLE);
               timePickerCancelButton.setVisibility(View.INVISIBLE);
           }
       });


       //Setting of dialog on restart button (Кнопка начинает привычку сначала)
       restartButton = (Button) findViewById(R.id.restartHabit);
       restartButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
               builder.setTitle(R.string.areYouSure);
               builder.setIcon(R.drawable.cryingcat);

             if (habit.star == SettingsHelper.silverStar )
               {builder.setMessage(R.string.restartProgressCanBeLostSilver);}
             else if (habit.star == SettingsHelper.goldStar )
               {builder.setMessage(R.string.restartProgressCanBeLostGold);}
             else
             { builder.setMessage(R.string.restartProgressCanBeLost); }


               builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       restartHabit(ID);
                       dialog.dismiss();
                   }
               });
               builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.dismiss();
                   }
               });
               builder.setCancelable(false);
               AlertDialog dialog = builder.create();
               dialog.show();
           }
       });

       //Установка обработчика кнопки для тестирования нотификация. Кнопка скрыта, и видима только в тестовой версии.
       Button testButton = (Button) findViewById(R.id.testNotification);
       testButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (v.getId() == R.id.testNotification) {
                   Notification notification;
                   NotificationCompat.Builder builder = new NotificationCompat.Builder(v.getContext());
                   builder.setOngoing(false);
                   builder.setAutoCancel(true);
                   builder.setContentTitle(String.valueOf(habitName));
                   builder.setContentText(getString(R.string.notificationHeader));
                   builder.setSmallIcon(R.mipmap.ic_launcher);
                   Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), habit.getPhotoID());
                   Bitmap resizedLargeIcon = Bitmap.createScaledBitmap(largeIcon , 100, 100, false);
                   builder.setLargeIcon(resizedLargeIcon);
                   { builder.setDefaults(Notification.DEFAULT_SOUND); }
                   Intent mainIntent = new Intent(v.getContext(), CardDetails.class);
                   mainIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                   mainIntent.putExtra("ID",ID);
                   mainIntent.setAction("Hello");
                   PendingIntent pendingMainIntent = PendingIntent.getActivity(v.getContext(), ID, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                   builder.setContentIntent(pendingMainIntent);
                   notification = builder.build(); //Intent with notification settings is packed into notification
                   Intent alarmIntent = new Intent(v.getContext(), AlarmHelper.class);
                   alarmIntent.putExtra("notification", notification); //notification is put into AlarmHelper intent
                   alarmIntent.putExtra("id", ID);
                   alarmIntent.setAction("Hello");
                   PendingIntent pendingIntent = PendingIntent.getBroadcast(CardDetails.this, ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                   //Initialization of Android Alarm Manager which calls notification
                   AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                   int seconds = 0;
                   int hoursInPicker   = timePicker.getCurrentHour();
                   int minutesInPicker = timePicker.getCurrentMinute();

                   Calendar calendar = Calendar.getInstance();
                   calendar.setTimeInMillis(System.currentTimeMillis());
                   calendar.set(Calendar.HOUR_OF_DAY, hoursInPicker);
                   calendar.set(Calendar.MINUTE, minutesInPicker);
                   calendar.set(Calendar.SECOND, seconds);
                   //adding leading zeroes
                   if (minutesInPicker > 9)
                   {stringMinutes = String.valueOf(minutesInPicker);}
                   else
                   {stringMinutes = "0"+String.valueOf(minutesInPicker);}

                   //Manager which send to standard Alarm Service Intent with Alarm Helper (periodically)
                   manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                   Toast.makeText(v.getContext(), getString(R.string.notificationSetAt)+" "+String.valueOf(hoursInPicker)+":"+stringMinutes, Toast.LENGTH_SHORT).show();
               }
           }
       });
}

//**********************************************************
//*************Handling of 'Activate/Deactivate' checkbox****
//***********************************************************
    public void onCheckboxClicked(View view) throws InterruptedException {

        //Проверяем достигнута ли звезда
         starAchieved = checkForStar(ID, context);
        if (starAchieved == SettingsHelper.goldStar)
        {
            showGoldDialog(habitName, context, ID);
            return;
        }
        else if (starAchieved == SettingsHelper.silverStar)
        {
            showSilverDialog(habitName, context, ID);
            return;
        }

        //Нельзя активировать более 3 привычек одновременно. Проверяем это.
        if (chbActive.isChecked())
        {
            activeHabitsNumber = dbHelper.getActiveHabitsNumber();
            if (activeHabitsNumber >= SettingsHelper.maxHabitsNumber)
            {
                Toast.makeText(this, R.string.threeHabits,  Toast.LENGTH_SHORT).show();
                chbActive.setChecked(false);
                return;
            }

            //Обрабатываем активацию привычки (вносим изменения в БД, показываем сообщения)
            handleActiveChecboxSet();
        }

         else   //Deactivate habit
        {
            if (habit.status == SettingsHelper.statusActiveNonStop) {
                //Отображаем диалог "ВЫ УВЕРЕНЫ?" если привычка выполняется без пауз
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setCancelable(false);
                builder.setTitle(R.string.areYouSure);
                builder.setMessage(R.string.areYouSureGoldMessage);
                builder.setIcon(R.drawable.cryingcat);

                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        handleActiveCheckboxUnset();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                        chbActive.setChecked(true);
                        return;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else
            {
                //Обрабатываем ДЕактивацию привычки (вносим изменения в БД, показываем сообщения)
                handleActiveCheckboxUnset();
            }
         }
      }


//**********************************************************
//*************Alarm Checkbox handling****
//***********************************************************
//Обрабатываем активацию/деактивацию чекбокса "Напоминание"
    public void onAlarmClicked(View view) throws InterruptedException {
        chbAlarm = (CheckBox) view;
        if ( chbAlarm.isChecked()) //Alarm checkbox is set
        {   //Check in notifications enabled in settings
            if (!settingsHelper.getNotificationSetting(context))
            {  chbAlarm.setChecked(false);
                Toast.makeText(this, R.string.allowNotifications, Toast.LENGTH_SHORT).show();
                return;
            }

            chbAlarm.setChecked(true);

            timePicker.setVisibility(View.VISIBLE);
            timePickerConfirmButton.setVisibility(View.VISIBLE);
            timePickerCancelButton.setVisibility(View.VISIBLE);

            timePicker.setEnabled(true);
            timePickerConfirmButton.setEnabled(false);
            timePickerCancelButton.setEnabled(false);

            //Устанавливаем текущее время в качестве исходного
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            hoursCurrent = calendar.get(Calendar.HOUR_OF_DAY);
            minutesCurrent = calendar.get(Calendar.MINUTE);

            timePicker.setCurrentHour(hoursCurrent);
            timePicker.setCurrentMinute(minutesCurrent);

            //Кнопки недоступны до первой прокрутки таймера
            timePickerConfirmButton.setEnabled(false);
            timePickerCancelButton.setEnabled(false);

            Toast toast = Toast.makeText(this, R.string.notificationSetTime, Toast.LENGTH_SHORT);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            v.setTextColor(getResources().getColor(R.color.white));
            toast.show();

         }
        else //********** Alarm checkbox is switched off************
        {
            chbAlarm.setChecked(false);
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            hoursCurrent = calendar.get(Calendar.HOUR_OF_DAY);
            minutesCurrent = calendar.get(Calendar.MINUTE);
            timePicker.setCurrentHour(hoursCurrent);
            timePicker.setCurrentMinute(minutesCurrent);
            timePicker.setEnabled(false);

            //Кнопки исчезают
            timePickerConfirmButton.setVisibility(View.INVISIBLE);
            timePickerCancelButton.setVisibility(View.INVISIBLE);

            dbHelper.updateHabitAlarmStatus(db, ID, SettingsHelper.alarmStatusNotActive);
            habit = dbHelper.getRecord(db, ID);

            //Отмена активного уведомления
            AlarmHelper alarmHelper = new AlarmHelper();
            alarmHelper.cancelNotification(context, ID);

            Toast toast = Toast.makeText(context, R.string.notificationDeactivated, Toast.LENGTH_SHORT);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            v.setTextColor(context.getResources().getColor(R.color.white));
            toast.show();
        }
    }

    @Override
    protected void onPause()
    {
        dbHelper.close();
        super.onPause();
    }

    //В методе инициализируются статусы и данные, происходит проверка на наличие звезд.
    //Этот метод выбран для данных операций т.к. он вызывается часто. А значит экран всегда будет отображать актуальное состояние привычки
    //Перфоманс программы от этого не пострадал
   @Override
  protected void onResume() {
       context = this;
       dbHelper = new DBHelper(this);
       db = dbHelper.getWritableDatabase();
       settingsHelper = new SettingsHelper();

       starAchieved = checkForStar(ID, context);
       if (starAchieved == SettingsHelper.goldStar)
       { showGoldDialog(habitName, context, ID);  }
       else if (starAchieved == SettingsHelper.silverStar)
       {  showSilverDialog(habitName, context, ID);   }

       // ********** UI initialization for New Habits *********
       if (habit.status == SettingsHelper.statusNew )
       {
           setNewState();
       }

       // ********** UI initialization for Paused Habits *********
       if (habit.status == SettingsHelper.statusPaused  )
       {
           setPausedState();
       }

       //********** UI initialization for Active without stops habits ********
       if (habit.status == SettingsHelper.statusActiveNonStop)
       {
           if (habit.star == SettingsHelper.noStar)
           {setActiveNonStopState();}
           else if (habit.star == SettingsHelper.silverStar)
           {setActiveSilverState();}
       }

       //********** UI initialization for Active WITH stops habits ********
       if (habit.status == SettingsHelper.statusActiveResumed)
       {
           setActiveResumedState();
       }

       //********** UI initialization for Silver Habits**********
       if (habit.status ==  SettingsHelper.statusCompletedSilver) //completed habit
       {
           setCompletedSilverState();
       }

       //********** UI initialization for Gold Habits**********
       if (habit.status ==  SettingsHelper.statusCompletedGold) //completed habit
       {
           setCompletedGoldState();
       }

       super.onResume();
   }

//Метод для отобржания скрытых данных
  public void onDetailsClicked(View v)
  {
          docView = (DocumentView) findViewById(R.id.habit_detailed_description_2);
          docView.setVisibility(View.VISIBLE);
          docView = (DocumentView) findViewById(R.id.habit_detailed_description_3);
          docView.setVisibility(View.VISIBLE);
          tvLable = (TextView) findViewById(R.id.tvOptional);
          tvLable.setVisibility(View.VISIBLE);
          tvLable = (TextView) findViewById(R.id.tvGeneralInfo);
          tvLable.setVisibility(View.VISIBLE);
          tvLable = (TextView) findViewById(R.id.tvHide);
          tvLable.setVisibility(View.VISIBLE);
          tvLable = (TextView) findViewById(R.id.tvDetails);
          tvLable.setVisibility(View.GONE);
  }

  //Метод для скрытия избыточных данных
    public void onHideClicked(View v)
    {
        docView = (DocumentView) findViewById(R.id.habit_detailed_description_2);
        docView.setVisibility(View.GONE);
        docView = (DocumentView) findViewById(R.id.habit_detailed_description_3);
        docView.setVisibility(View.GONE);
        tvLable = (TextView) findViewById(R.id.tvOptional);
        tvLable.setVisibility(View.GONE);
        tvLable = (TextView) findViewById(R.id.tvGeneralInfo);
        tvLable.setVisibility(View.GONE);
        tvLable = (TextView) findViewById(R.id.tvHide);
        tvLable.setVisibility(View.GONE);
        tvLable = (TextView) findViewById(R.id.tvDetails);
        tvLable.setVisibility(View.VISIBLE);
    }

    //Метод для проверки "Не наступло ли еще время присваивать звезду?"
    public int  checkForStar(int ID,  Context context)
    {
        DBHelper dbHelper;
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        int starAchieved = SettingsHelper.noStar;
        habit = dbHelper.getRecord(db, ID);

        long currentTime = System.currentTimeMillis();
        long unbrokenTime = currentTime - habit.getTimeStamp();
        long sumTime = habit.getTime() + unbrokenTime;

        if (unbrokenTime >= SettingsHelper.aimTime)
        {
           if (habit.status == SettingsHelper.statusActiveNonStop || habit.status == SettingsHelper.statusActiveResumed)
           {
               starAchieved = SettingsHelper.goldStar;
           }
        }
        else if (sumTime >= SettingsHelper.aimTime )
        {
          if (habit.status == SettingsHelper.statusActiveResumed)
          {
                   starAchieved = SettingsHelper.silverStar;
           }
        }
             return starAchieved;
       }

       //Метод для отображения диалога о присвоении золотой звездочки
    public void showGoldDialog(String name, final Context context, final int ID)
    {

        long doneTime = SettingsHelper.aimTime;
        long timeToSetDoneTime =  SettingsHelper.aimTime / SettingsHelper.timeRate;
        String unitDone = determineUnit(doneTime, SettingsHelper.timeRate, context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.congratulationsTitle));


        if (dbHelper.getGoldStarsNumber() == 0)
        {
            builder.setMessage(
                              context.getString(R.string.congratulationsYouManagedToComplete)
                            + " \"" + name +"\""
                            + "\n"
                            + "\n"
                            + context.getString(R.string.congratulationsYouHaveWorked) + " "
                            + String.valueOf(timeToSetDoneTime) + " " + unitDone + " "
                            + "\n"
                            + "\n"
                            + context.getString(R.string.congratulationsGoldTextFirst)+ "! "
                            + "\n"
                            + "\n"
                            + context.getString(R.string.congratulationsDontForget)+ "! "

            );
 }
        else
        {
            builder.setMessage(
                    context.getString(R.string.congratulationsYouManagedToComplete)
                            + " \"" + name +"\""
                            + "\n"
                            + "\n"
                            + context.getString(R.string.congratulationsYouHaveWorked) + " "
                            + String.valueOf(timeToSetDoneTime) + " " + unitDone + " "
                            + "\n"
                            + "\n"
                            + context.getString(R.string.congratulationsGoldText)+ "! "
                            + "\n"
                            + "\n"
                            + context.getString(R.string.congratulationsDontForget)+ "! "

            );
        }

//        builder.setMessage(context.getString(R.string.congratulations21Gold)+ " \"" + name +"\". "+context.getString(R.string.congratulationsGoldText));
        builder.setIcon(R.drawable.stargold);
        builder.setCancelable(false);

        //Обработчик нажатия кнопки
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dbHelper.updateStar(db, ID, SettingsHelper.goldStar);
                dbHelper.increaseGoldStarsNumber();

                dbHelper.updateHabitStatus(db, ID, SettingsHelper.statusCompletedGold);
                dbHelper.updateTimeTimeStamp(db, ID, 0, 0);
                dbHelper.updateHabitAlarmStatus(db, ID, SettingsHelper.alarmStatusNotActive);
                dbHelper.decreaseActiveHabitsNumber();
                AlarmHelper alarmHelper = new AlarmHelper();
                alarmHelper.cancelNotification(context, ID);

                dialog.dismiss();
                setCompletedGoldState();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Метод для отображения серебряного диалога
    void showSilverDialog(String name, final Context context, final int ID)
    {
        long currentTime = System.currentTimeMillis();

        long doneTime = SettingsHelper.aimTime;
        long timeToSetDoneTime =  SettingsHelper.aimTime / SettingsHelper.timeRate;
        String unitDone = determineUnit(doneTime, SettingsHelper.timeRate, context);

        long leftTime = SettingsHelper.timeRate + SettingsHelper.aimTime - (currentTime - habit.getTimeStamp());
        long timeToSetLeftToGold =  leftTime / SettingsHelper.timeRate;
        String unitToSetLeft = determineUnit(leftTime, SettingsHelper.timeRate, context);

        long unbrokenTime = SettingsHelper.timeRate + doneTime - leftTime;
        long timeToSetUnbrokenTime =  unbrokenTime / SettingsHelper.timeRate;
        String unitUnbrokenTime = determineUnit(unbrokenTime, SettingsHelper.timeRate, context);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.congratulationsTitle));
   if (dbHelper.getSilverStarsNumber() == 0)
     {
       builder.setMessage(
                           context.getString(R.string.congratulationsYou)+" "
                         + String.valueOf(timeToSetDoneTime) + " " + unitDone  + " "
                         + context.getString(R.string.congratulationsWorkWithHabit)+" "
                         + "\"" + name + "\""
                         + "\n"
                         + "\n"
                          + context.getString(R.string.congratulationsSilverTextFirst)+ "!"
                         + "\n"
                         + "\n"
                         + context.getString(R.string.congratulationsWorkUnbroken)+ " "
                         + String.valueOf(timeToSetUnbrokenTime ) + " " + unitUnbrokenTime + ", "
                         + context.getString(R.string.congratulationsSilverLeftToGold)+ " "
                         + String.valueOf(timeToSetLeftToGold ) + " " + unitToSetLeft + " "
                         + context.getString(R.string.congratulationsUnbrokenWork)+ " "
                         + "\n"
                         + "\n"
                         + context.getString(R.string.congratulationsSilverQuestion)
                           );
     }
    else
     {
         builder.setMessage(
                 context.getString(R.string.congratulationsYou)+" "
                         + String.valueOf(timeToSetDoneTime) + " " + unitDone  + " "
                         + context.getString(R.string.congratulationsWorkWithHabit)+" "
                         + "\"" + name + "\""
                         + "\n"
                         + "\n"
                         + context.getString(R.string.congratulationsSilverText)+ "!"
                         + "\n"
                         + "\n"
                         + context.getString(R.string.congratulationsWorkUnbroken)+ " "
                         + String.valueOf(timeToSetUnbrokenTime ) + " " + unitUnbrokenTime + ", "
                         + context.getString(R.string.congratulationsSilverLeftToGold)+ " "
                         + String.valueOf(timeToSetLeftToGold ) + " " + unitToSetLeft + " "
                         + context.getString(R.string.congratulationsUnbrokenWork)+ " "
                         + "\n"
                         + "\n"
                         + context.getString(R.string.congratulationsSilverQuestion)
         );
      }

        builder.setIcon(R.drawable.starsilver);
        builder.setCancelable(false);

        //Обработчик позитивной кнопки
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                long currentTime = System.currentTimeMillis();
                long unbrokenTime = currentTime - habit.getTimeStamp();

                dbHelper.updateStar(db, ID, SettingsHelper.silverStar);
                dbHelper.increaseSilverStarsNumber();

                dbHelper.updateHabitStatus(db, ID, SettingsHelper.statusActiveNonStop);
                dbHelper.updateTimeTimeStamp(db, ID, 0, currentTime-unbrokenTime);

                dialog.dismiss();
                setActiveSilverState();
                 }
        });

        //Обработчик негативной кнопки
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbHelper.updateStar(db, ID, SettingsHelper.silverStar);
                dbHelper.increaseSilverStarsNumber();

                dbHelper.updateHabitStatus(db, ID, SettingsHelper.statusCompletedSilver);
                dbHelper.updateTimeTimeStamp(db, ID, 0, 0);
                dbHelper.updateHabitAlarmStatus(db, ID, SettingsHelper.alarmStatusNotActive);
                dbHelper.decreaseActiveHabitsNumber();

                AlarmHelper alarmHelper = new AlarmHelper();
                alarmHelper.cancelNotification(context, ID);

                dialog.dismiss();
                setCompletedSilverState();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

   //Обработчик кнопки "Заново". Все статусы привычки возвращаются к исходному состоянию.
    private void restartHabit(int id)
    {
        dbHelper.updateTimeTimeStamp(db, id, 0, 0);

        dbHelper.updateStar(db, id, SettingsHelper.noStar);
        if (habit.star == SettingsHelper.silverStar)
        {  dbHelper.decreaseSilverStarsNumber();  }
        else if (habit.star == SettingsHelper.goldStar)
        { dbHelper.decreaseGoldStarsNumber();   }

        dbHelper.updateHabitStatus(db,id, SettingsHelper.statusNew);
        dbHelper.updateHabitAlarmStatus(db, id, SettingsHelper.alarmStatusNotActive);

        if (habit.status == SettingsHelper.statusActiveNonStop || habit.status == SettingsHelper.statusActiveResumed)
        { dbHelper.decreaseActiveHabitsNumber(); }

        habit = dbHelper.getRecord(db, id);

        setNewState();
        Toast.makeText(this, R.string.habitTrackerReset, Toast.LENGTH_SHORT).show();
    }

    //Иницилизируем таймер
    private void setTimePicker()
    {
        timePicker = (TimePicker) findViewById(R.id.timePicker1);
        timePickerConfirmButton = (Button) findViewById(R.id.timePickerConfirm);
        timePickerCancelButton = (Button) findViewById(R.id.timePickerCancel);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hoursCurrent = calendar.get(Calendar.HOUR_OF_DAY);
        int minutesCurrent = calendar.get(Calendar.MINUTE);

        if (habit.alarmstatus == SettingsHelper.alarmStatusActive) {
            timePicker.setCurrentHour(habit.alarmhours);
            timePicker.setCurrentMinute(habit.alarmminutes);
        }
        else
        {
            timePicker.setCurrentHour(hoursCurrent);
            timePicker.setCurrentMinute(minutesCurrent);
        }
        timePickerConfirmButton.setVisibility(View.INVISIBLE);
        timePickerCancelButton.setVisibility(View.INVISIBLE);
    }

    //Иницилизируем UI для привычек в статусе StatusNew (см SettingsHelper constants)
    private void setNewState() //#setnewstate
    {
        habit = dbHelper.getRecord(db, ID);
        setTimePicker();

        relativeLayout = (RelativeLayout) findViewById(R.id.rlProgressDays);
        layoutParams =  (RelativeLayout.LayoutParams)relativeLayout.getLayoutParams();
        layoutParams.rightMargin = 0-80;

        tvRunningText = (TextView) findViewById(R.id.tvRuningText);

        chbActive.setChecked(false);
        chbAlarm.setChecked(false);
        chbActive.setEnabled(true);
        chbAlarm.setEnabled(false);
        chbActive.setText(R.string.begin);
        timePicker.setEnabled(false);

        tvStar.setBackgroundResource(0);
        tvState.setBackgroundResource(0);

        tvDaysLeft.setVisibility(View.INVISIBLE);
        tvLableDaysLeft.setVisibility(View.INVISIBLE);
        ivStarLeft.setVisibility(View.INVISIBLE);
        tvDaysLeftWordDays.setVisibility(View.INVISIBLE);
        timePickerConfirmButton.setVisibility(View.INVISIBLE);
        timePickerCancelButton.setVisibility(View.INVISIBLE);

        ivStarBig.setVisibility(View.GONE);

        tvLableHabitDays.setVisibility(View.VISIBLE);
        tvLableHabitDays.setTextColor(getResources().getColor(R.color.grey));
        tvLableHabitDays.setText(R.string.workWithHabit);

        tvHabit = (TextView) findViewById(R.id.habit_name_l);
        tvHabit.setTextColor(getResources().getColor(R.color.darkdarkGrey));
        tvHabit = (TextView) findViewById(R.id.habit_days_l);
        tvHabit.setTextColor(getResources().getColor(R.color.darkdarkGrey));

        long timeToSet = habit.getTime();
        if (timeToSet >= SettingsHelper.aimTime) {timeToSet = SettingsHelper.aimTime - SettingsHelper.timeRate;}
        tvHabit = (TextView) findViewById(R.id.habit_days_l) ;
        tvHabit.setText(String.valueOf(timeToSet/ SettingsHelper.timeRate));

        unitsToSet = determineUnit(timeToSet, SettingsHelper.timeRate, context);
        tvDaysHabitWordDays.setText(unitsToSet);

        tvLableHabitDays.setText(R.string.workWithHabit);

        tvRunningText.setTextColor(getResources().getColor(R.color.pickerblue));
     }

    //Иницилизируем UI для привычек в статусе StatusPaused (см SettingsHelper constants)
    private void setPausedState()
    {
        setNewState();

        relativeLayout = (RelativeLayout) findViewById(R.id.rlProgressDays);
        layoutParams =  (RelativeLayout.LayoutParams)relativeLayout.getLayoutParams();
        layoutParams.rightMargin = 0;

        tvDaysLeft.setVisibility(View.VISIBLE);
        tvLableDaysLeft.setVisibility(View.VISIBLE);
        ivStarLeft.setVisibility(View.VISIBLE);
        tvDaysLeftWordDays.setVisibility(View.VISIBLE);

        tvState.setBackgroundResource(R.drawable.icon_paused);

        ivStarLeft.setBackgroundResource(R.drawable.starsilver);

        chbActive.setText(R.string.resume);


        //Calculate time to set
        long currentTime = System.currentTimeMillis();
        long timeToSet = habit.getTime();
        long timeLeft = SettingsHelper.aimTime + SettingsHelper.timeRate - timeToSet;


        if (timeToSet > SettingsHelper.aimTime)
        {timeToSet = SettingsHelper.aimTime-SettingsHelper.timeRate;
         timeLeft =  SettingsHelper.aimTime - timeToSet;
        }

        tvDaysLeft.setText(String.valueOf(timeLeft / SettingsHelper.timeRate));

        unitsToSet = determineUnit(timeLeft, SettingsHelper.timeRate, context);
        tvDaysLeftWordDays.setText(unitsToSet);

        if ( (timeLeft/SettingsHelper.timeRate) == 21 || (timeLeft/SettingsHelper.timeRate) == 1 )
        tvLableDaysLeft.setText(R.string.lableLeftTo21);
        else
        tvLableDaysLeft.setText(R.string.lableLeftTo);

      }

    //Иницилизируем UI для привычек в статусе StatusActiveNonStop (см SettingsHelper constants)
    //Привычка активна и ни разу не ставилась на паузу
    private void setActiveNonStopState()
    {
        relativeLayout = (RelativeLayout) findViewById(R.id.rlProgressDays);
        layoutParams =  (RelativeLayout.LayoutParams)relativeLayout.getLayoutParams();
        layoutParams.rightMargin = 0;

        tvDaysLeft.setVisibility(View.VISIBLE);
        tvLableDaysLeft.setVisibility(View.VISIBLE);
        ivStarLeft.setVisibility(View.VISIBLE);
        tvDaysLeftWordDays.setVisibility(View.VISIBLE);

        ivStarBig.setVisibility(View.GONE);

        tvLableHabitDays.setText(R.string.workWithHabit);

        chbActive.setChecked(true);
        chbAlarm.setChecked(false);
        chbActive.setEnabled(true);
        chbAlarm.setEnabled(true);
        chbActive.setText(R.string.begin);

        habit = dbHelper.getRecord(db, ID);
        setTimePicker();

        //Alarm elements setting
        if (habit.alarmstatus ==  SettingsHelper.alarmStatusActive )  //Enable alarm
        {
            chbAlarm.setChecked(true);
            timePicker.setVisibility(View.VISIBLE);
            timePicker.setEnabled(true);
            timePickerConfirmButton.setVisibility(View.INVISIBLE);
            timePickerConfirmButton.setEnabled(false);
            timePickerCancelButton.setVisibility(View.INVISIBLE);
            timePickerCancelButton.setEnabled(false);
        }
        else
        {
            timePicker.setEnabled(false);
            timePickerConfirmButton.setVisibility(View.INVISIBLE);
            timePickerCancelButton.setVisibility(View.INVISIBLE);
        }

        timePickerConfirmButton.setEnabled(false);
        timePickerCancelButton.setEnabled(false);

        tvState.setVisibility(View.VISIBLE);
        tvState.setBackgroundResource(R.drawable.icon_play);
        tvStar.setBackgroundResource(0);

        ivStarLeft.setBackgroundResource(R.drawable.stargold);

        //Calculate time to set
        long currentTime = System.currentTimeMillis();
        long timeToSet = habit.getTime() + currentTime - habit.getTimeStamp();
        long timeleft = SettingsHelper.aimTime + SettingsHelper.timeRate - timeToSet;
        if (timeToSet > SettingsHelper.aimTime)
        {timeToSet = SettingsHelper.aimTime-SettingsHelper.timeRate;
         timeleft = SettingsHelper.aimTime - timeToSet;
        }

        tvHabit = (TextView) findViewById(R.id.habit_name_l);
        tvHabit.setTextColor(getResources().getColor(R.color.darkGreenText));

        tvHabit = (TextView) findViewById(R.id.habit_days_l);
        tvHabit.setText(String.valueOf(timeToSet / SettingsHelper.timeRate));

        unitsToSet = determineUnit(timeToSet, SettingsHelper.timeRate, context);
        tvDaysHabitWordDays.setText(unitsToSet);

        tvHabit.setTextColor(getResources().getColor(R.color.darkGreenText));

        tvDaysLeft.setText(String.valueOf((timeleft) / SettingsHelper.timeRate));
        unitsToSet = determineUnit(timeleft, SettingsHelper.timeRate, context);
        tvDaysLeftWordDays.setText(unitsToSet);

        if ( (timeleft/SettingsHelper.timeRate) == 21 || (timeleft/SettingsHelper.timeRate) == 1 )
            tvLableDaysLeft.setText(R.string.lableLeftTo21);
        else
            tvLableDaysLeft.setText(R.string.lableLeftTo);

        tvRunningText = (TextView) findViewById(R.id.tvRuningText) ;
        tvRunningText.setTextColor(getResources().getColor(R.color.pickerblue));
     }

    //Иницилизируем UI для привычек в статусе StatusActiveResumed (см SettingsHelper constants)
    //Привычка активна, однако как минимум один раз ставилась на паузу
    private void setActiveResumedState()
    {
        setActiveNonStopState();

        chbActive.setText(R.string.resume);
        tvState.setBackgroundResource(R.drawable.icon_resumed);

        long currentTime = System.currentTimeMillis();
        long timeUnbroken = currentTime - habit.getTimeStamp();
        if (timeUnbroken >= SettingsHelper.aimTime) {timeUnbroken = SettingsHelper.aimTime-SettingsHelper.timeRate;}

        tvLableHabitDays.setVisibility(View.VISIBLE);

        tvLableHabitDays.setTextColor(getResources().getColor(R.color.grey));
        ivStarLeft.setBackgroundResource(R.drawable.starsilver);
    }

    //Иницилизируем UI для привычек в активном статусе и серебряной звездой
    private void setActiveSilverState()
    {
        setActiveNonStopState();
        tvStar.setBackgroundResource(R.drawable.starsilver);
        tvStar.setVisibility(View.VISIBLE);
        ivStarBig.setVisibility(View.GONE);
        chbActive.setText(R.string.begin);
    }

    //Иницилизируем UI для привычек в статусе StatusCompletedSilver(см SettingsHelper constants)
    private void setCompletedSilverState()
    {
        setNewState();

        relativeLayout = (RelativeLayout) findViewById(R.id.rlProgressDays);
        layoutParams =  (RelativeLayout.LayoutParams)relativeLayout.getLayoutParams();
        layoutParams.rightMargin = 0;

        tvDaysLeft.setVisibility(View.GONE);
        tvLableDaysLeft.setVisibility(View.GONE);
        ivStarLeft.setVisibility(View.GONE);
        tvDaysLeftWordDays.setVisibility(View.GONE);

        tvStar = (TextView) findViewById(R.id.habit_star);
       // tvStar.setBackgroundResource(R.drawable.starsilver);
        tvStar.setVisibility(View.INVISIBLE);

        chbActive.setText(R.string.begin);
        tvLableHabitDays.setText(R.string.workWithHabitWithPauses);
        tvHabit = (TextView) findViewById(R.id.habit_days_l) ;
        tvHabit.setText(String.valueOf(SettingsHelper.aimTime / SettingsHelper.timeRate ));
        unitsToSet = determineUnit(SettingsHelper.aimTime, SettingsHelper.timeRate, context);
        tvDaysHabitWordDays.setText(unitsToSet);
        ivStarBig.setVisibility(View.VISIBLE);
        ivStarBig.setBackgroundResource(R.drawable.starsilver);
    }

    //Иницилизируем UI для привычек в статусе StatusCompletedGold(см SettingsHelper constants)
    private void setCompletedGoldState()
    {
        habit = dbHelper.getRecord(db, ID);
        setTimePicker();

        chbActive.setChecked(false);
        chbAlarm.setChecked(false);
        chbActive.setEnabled(false);
        chbAlarm.setEnabled(false);
        chbActive.setText(R.string.begin);
        timePicker.setEnabled(false);

        tvDaysLeft.setVisibility(View.GONE);
        tvLableDaysLeft.setVisibility(View.GONE);
        ivStarLeft.setVisibility(View.GONE);
        tvDaysLeftWordDays.setVisibility(View.GONE);

        timePickerConfirmButton.setVisibility(View.INVISIBLE);
        timePickerCancelButton.setVisibility(View.INVISIBLE);

        tvLableHabitDays.setVisibility(View.VISIBLE);
        tvLableHabitDays.setTextColor(getResources().getColor(R.color.grey));

        ivStarBig.setVisibility(View.VISIBLE);
        ivStarBig.setBackgroundResource(R.drawable.stargold);

        //tvStar.setBackgroundResource(R.drawable.stargold);
        tvStar.setVisibility(View.INVISIBLE);


        tvState.setBackgroundResource(0);
//        tvStateUnbroken.setBackgroundResource(0);

        tvHabit = (TextView) findViewById(R.id.habit_name_l);
        tvHabit.setTextColor(Color.rgb(255,215,0)); //gold
        tvHabit = (TextView) findViewById(R.id.habit_days_l);
        tvHabit.setTextColor(Color.rgb(255,215,0));  //gold
        tvRunningText.setTextColor(getResources().getColor(R.color.pickerblue));

        tvHabit = (TextView) findViewById(R.id.habit_days_l) ;
        tvHabit.setText(String.valueOf(SettingsHelper.aimTime / SettingsHelper.timeRate ));
        unitsToSet = determineUnit(SettingsHelper.aimTime, SettingsHelper.timeRate, context);
        tvDaysHabitWordDays.setText(unitsToSet);

        tvDaysLeft.setText("0");
        tvLableHabitDays.setText(R.string.workWithHabitUnbroken);

        ivStarLeft.setBackgroundResource(R.drawable.stargold);
    }


    //Вносим изменения в БД и отображаем сообщения если чекбокс активации/деактивации активирован
    private void handleActiveChecboxSet()
    {
        dbHelper.increaseActiveHabitsNumber();

        long currentTime = System.currentTimeMillis();

        if (habit.status == SettingsHelper.statusNew)
        {
            dbHelper.updateHabitStatus(db, ID,  SettingsHelper.statusActiveNonStop);
            dbHelper.updateTimeStamp(db, ID, currentTime);
            habit = dbHelper.getRecord(db, ID);
            setActiveNonStopState();
            Toast.makeText(this, R.string.habitActivated, Toast.LENGTH_SHORT).show();
        }
        else if (habit.status == SettingsHelper.statusPaused)
        {
            dbHelper.updateHabitStatus(db, ID,  SettingsHelper.statusActiveResumed);
            dbHelper.updateTimeStamp(db, ID, currentTime);
            habit = dbHelper.getRecord(db, ID);
            setActiveResumedState();
            Toast.makeText(this, R.string.habitResumed, Toast.LENGTH_SHORT).show();
        }
        else if (habit.status == SettingsHelper.statusCompletedSilver)
        {
            dbHelper.updateHabitStatus(db, ID,  SettingsHelper.statusActiveNonStop);
            dbHelper.updateTimeStamp(db, ID, currentTime);
            habit = dbHelper.getRecord(db, ID);
            setActiveSilverState();
            Toast.makeText(this, R.string.habitStartToGold, Toast.LENGTH_SHORT).show();
        }
    }

    //Вносим изменения в БД и отображаем сообщения если чекбокс активации/деактивации деактивирован
    public void handleActiveCheckboxUnset()
    {
        if (chbAlarm.isChecked()) //Deactivate notification if set
        {
            chbAlarm.setChecked(false);
            dbHelper.updateHabitAlarmStatus(db, ID, SettingsHelper.alarmStatusNotActive);
            AlarmHelper alarmHelper = new AlarmHelper();
            alarmHelper.cancelNotification(context, ID);

            Toast toast = Toast.makeText(context, R.string.notificationDeactivated, Toast.LENGTH_SHORT);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            v.setTextColor(context.getResources().getColor(R.color.white));
            toast.show();
        }

        if (habit.star == SettingsHelper.silverStar)
        {
            dbHelper.updateHabitStatus(db, ID, SettingsHelper.statusCompletedSilver);
            dbHelper.decreaseActiveHabitsNumber();
            dbHelper.updateTimeTimeStamp(db, ID, 0, 0);
            habit = dbHelper.getRecord(db, ID);
            setCompletedSilverState();
            Toast.makeText(this, R.string.habitGoldSuspended, Toast.LENGTH_SHORT).show();
        }
        else
        {
            dbHelper.updateHabitStatus(db, ID, SettingsHelper.statusPaused);
            dbHelper.decreaseActiveHabitsNumber();
            habit = dbHelper.getRecord(db, ID);

            long currentTime = System.currentTimeMillis();
            long passedTime = currentTime - habit.timestamp;
            long totalTime = habit.time + passedTime;
            dbHelper.updateTimeTimeStamp(db, ID, totalTime, 0);
            habit = dbHelper.getRecord(db, ID);

            setPausedState();
            Toast.makeText(this, R.string.habitSuspended, Toast.LENGTH_SHORT).show();
        }
    }

    //Метод, который по числу дней (часов) определяет падеж слова (день, дней, дня, часов, часа, час, минут, минута, минуты....)
    public String determineUnit(long timeToSet, long timeRate, Context context )
    {
        String unitWord = "";
        String value = "";
        char lastDigit =' ';
        char beforeLastDigit= ' ';
//        if (timeRate == SettingsHelper.timeRateinDays || timeRate == SettingsHelper.timeRateinMinutes )
//        {
//            if ( (timeToSet / timeRate)  == 1 || (timeToSet / timeRate) == 21 )
//            {unitWord = getResources().getString(R.string.days1);}
//            else if ( (timeToSet / timeRate)  >= 2 && (timeToSet / timeRate) <= 4 )
//            {unitWord = getResources().getString(R.string.days2_4);}
//            else if  ( (timeToSet / timeRate) == 0 || (timeToSet / timeRate)  >= 5 || (timeToSet / timeRate) <= 20 )
//            {unitWord = getResources().getString(R.string.days0_5_20);}
//            else
//            {unitWord = getResources().getString(R.string.days0_5_20);}
//          }

        if (timeRate == SettingsHelper.timeRateinDays  )
        {
            value = String.valueOf(timeToSet/timeRate);
            lastDigit = value.charAt(value.length() - 1 );
            if (value.length() == 1)
            {beforeLastDigit = '0';}
            else
            {  beforeLastDigit = value.charAt(value.length() - 2);  }

            switch (lastDigit){
                case '1':
                    if (beforeLastDigit == '1')
                    {unitWord = context.getResources().getString(R.string.days0_5_20);}
                    else
                    {unitWord = context.getResources().getString(R.string.days1);}
                    break;
                case '2':
                case '3':
                case '4':
                    if (beforeLastDigit == '1')
                    {unitWord = context.getResources().getString(R.string.days0_5_20);}
                    else
                    {unitWord = context.getResources().getString(R.string.days2_4);}
                    break;
                case '0':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    unitWord = context.getResources().getString(R.string.days0_5_20);
                    break;
                default:
                    unitWord = context.getResources().getString(R.string.days0_5_20);
            }
        }


        else if (timeRate == SettingsHelper.timeRateinHours )
        {
            value = String.valueOf(timeToSet/timeRate);
            lastDigit = value.charAt(value.length() - 1 );
            if (value.length() == 1)
            {beforeLastDigit = '0';}
                else
            {  beforeLastDigit = value.charAt(value.length() - 2);  }

            switch (lastDigit){
                case '1':
                    if (beforeLastDigit == '1')
                    {unitWord = context.getResources().getString(R.string.hours0_5_20);}
                    else
                    {unitWord = context.getResources().getString(R.string.hours1);}
                    break;
                case '2':
                case '3':
                case '4':
                    if (beforeLastDigit == '1')
                    {unitWord = context.getResources().getString(R.string.hours0_5_20);}
                    else
                    {unitWord = context.getResources().getString(R.string.hours2_4);}
                    break;
                case '0':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    unitWord = context.getResources().getString(R.string.hours0_5_20);
                    break;
                 default:
                     unitWord = getResources().getString(R.string.hours0_5_20);
            }
        }

        else if (timeRate == SettingsHelper.timeRateinMinutes )
        {
            value = String.valueOf(timeToSet/timeRate);
            lastDigit = value.charAt(value.length() - 1 );
            if (value.length() == 1)
            {beforeLastDigit = '0';}
            else
            {  beforeLastDigit = value.charAt(value.length() - 2);  }

            switch (lastDigit){
                case '1':
                    if (beforeLastDigit == '1')
                    {unitWord = context.getResources().getString(R.string.minutes0_5_20);}
                    else
                    {unitWord = context.getResources().getString(R.string.minutes1);}
                    break;
                case '2':
                case '3':
                case '4':
                    if (beforeLastDigit == '1')
                    {unitWord = context.getResources().getString(R.string.minutes0_5_20);}
                    else
                    {unitWord = context.getResources().getString(R.string.minutes2_4);}
                    break;
                case '0':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    unitWord = context.getResources().getString(R.string.minutes0_5_20);
                    break;
                default:
                    unitWord = context.getResources().getString(R.string.minutes0_5_20);
            }
        }


       else if (timeRate == SettingsHelper.timeRateinSeconds )
        {
            value = String.valueOf(timeToSet/timeRate);
            lastDigit = value.charAt(value.length() - 1 );
            if (value.length() == 1)
            {beforeLastDigit = '0';}
            else
            {  beforeLastDigit = value.charAt(value.length() - 2);  }

            switch (lastDigit){
                case '1':
                    if (beforeLastDigit == '1')
                    {unitWord = context.getResources().getString(R.string.seconds0_5_20);}
                    else
                    {unitWord = context.getResources().getString(R.string.seconds1);}
                    break;
                case '2':
                case '3':
                case '4':
                    if (beforeLastDigit == '1')
                    {unitWord = context.getResources().getString(R.string.seconds0_5_20);}
                    else
                    {unitWord = context.getResources().getString(R.string.seconds2_4);}
                    break;
                case '0':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    unitWord = context.getResources().getString(R.string.seconds0_5_20);
                    break;
                default:
                    unitWord = context.getResources().getString(R.string.seconds0_5_20);
            }
        }
        return unitWord;
    }

    //Метод, который фомирует уведомление и передает его в Android Notification System
    private void raiseNotification()
    {
        Notification notification;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setAutoCancel(false);

        builder.setContentTitle(String.valueOf(habitName));
        builder.setContentText(getString(R.string.notificationHeader));

        builder.setSmallIcon(R.mipmap.ic_launcher);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), habit.getPhotoID());
        Bitmap resizedLargeIcon = Bitmap.createScaledBitmap(largeIcon , 100, 100, false);
        builder.setLargeIcon(resizedLargeIcon);

        builder.setOngoing(false);
        builder.setAutoCancel(true);

        if (settingsHelper.getSoundSetting(context))
        { builder.setDefaults(Notification.DEFAULT_SOUND); }
        else
        {builder.setDefaults(Notification.DEFAULT_LIGHTS);}

        Intent mainIntent = new Intent(this, CardDetails.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        mainIntent.putExtra("ID",ID);
        mainIntent.setAction("Hello");
        PendingIntent pendingMainIntent = PendingIntent.getActivity(this, ID, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingMainIntent);
        notification = builder.build(); //Intent with notification settings is packed into notification
//            notification.contentView.setImageViewResource(android.R.id.icon, habit.photoId);

        Intent alarmIntent = new Intent(this, AlarmHelper.class);
        alarmIntent.putExtra("notification", notification); //notification is put into AlarmHelper intent
        alarmIntent.putExtra("id", ID);
        alarmIntent.setAction("Hello");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(CardDetails.this, ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Initialization of Android Alarm Manager which calls notification
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        hoursCurrent = calendar.get(Calendar.HOUR_OF_DAY);
        minutesCurrent = calendar.get(Calendar.MINUTE);
        int seconds = 0;

        int hoursInPicker   = timePicker.getCurrentHour();
        int minutesInPicker = timePicker.getCurrentMinute();

        calendar.set(Calendar.HOUR_OF_DAY, hoursInPicker);
        calendar.set(Calendar.MINUTE, minutesInPicker);
        calendar.set(Calendar.SECOND, seconds);

        //If time is passed
        if (hoursCurrent > hoursInPicker)
        {
//               calendar.add(Calendar.DATE, 1);
            calendar.add(Calendar.SECOND, SettingsHelper.notificationPeriod/1000);
        }
        //If hours are already come - check minutes
        else if (hoursCurrent == hoursInPicker)
        {      //currente minute already come or passed
            if (minutesCurrent >= minutesInPicker)
            {
                calendar.add(Calendar.SECOND, SettingsHelper.notificationPeriod/1000);
            }
        }

        //adding leading zeroes
        if (minutesInPicker > 9)
        {stringMinutes = String.valueOf(minutesInPicker);}
        else
        {stringMinutes = "0"+String.valueOf(minutesInPicker);}

        //Manager which send to standard Alarm Service Intent with Alarm Helper (periodically)
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), SettingsHelper.notificationPeriod, pendingIntent);
        Toast toast = Toast.makeText(this, getString(R.string.notificationSetAt)+" "+String.valueOf(hoursInPicker)+":"+stringMinutes, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(getResources().getColor(R.color.white));
        toast.show();
    }

    //Метод с помощью которого формируются правильные переносы текста
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        //Logic wich trancates size of TextView by longest word
        Rect bounds = new Rect();
        tvHabit = (TextView) findViewById(R.id.habit_name_l);
        int linesNumber = tvHabit.getLineCount();

        tvHabit.getPaint().getTextBounds(tvHabit.getText().toString(), 0, tvHabit.getText().length(), bounds);
        int habitNameWidth = bounds.width();

        String[] parts = habitName.split(" ");
        int longestWordLength = 1;
        int wordsNumber = 0;
        String longestWord = "";
        for (String s: parts)
        {
            wordsNumber = wordsNumber + 1;
            if(s.length() > longestWordLength)
            {longestWordLength = s.length();
                longestWord = s;
            }
        }

        tvHabit.setText(longestWord);
        tvHabit.getPaint().getTextBounds(tvHabit.getText().toString(), 0, tvHabit.getText().length(), bounds);
        int wordWidth = bounds.width();

        int widthToSet;
        if (linesNumber == 2)
        {
            if (wordsNumber<3)
            {tvHabit.setWidth(wordWidth+20);}
            else
            {tvHabit.setWidth(habitNameWidth-wordWidth+20);}
        }
        else if (linesNumber == 3)
        {
            {tvHabit.setWidth(wordWidth+20);}
        }

        tvHabit.setText(habitName);
    }



    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }


    public static Context getContext()
    {
        return context;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

}




