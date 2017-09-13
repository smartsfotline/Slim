package pt.personaltrainer.fragments;
//Главный фрагмент, отображающий список привычек

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

import pt.personaltrainer.AlarmHelper;
import pt.personaltrainer.CardDetails;
import pt.personaltrainer.DBHelper;
import pt.personaltrainer.Habits;
import pt.personaltrainer.R;
import pt.personaltrainer.RVAdapter;
import pt.personaltrainer.SettingsHelper;

public class FragmentHabits extends Fragment {
    //Стандартные атрибуты
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Объявление атрибутов класса
    public Context context;
    public List<Habits> habits;
    public DBHelper dbHelper;
    private AlertDialog goldPostponedDialog;
    private AlertDialog silverPostponedDialog;
    private LinearLayoutManager llm;
    public  RVAdapter rvadapter;
    private OnFragmentInteractionListener mListener;

    //Объявление переменных
    int i = 0;
    int dialogCounter = 0;
    int silverDialogCounter = 0;
    int goldDialogCounter = 0;
    Handler handler;
    //final int aimTime = 21; //30240;

    SettingsHelper settingsHelper = new SettingsHelper();

    //Кусок стандартного кода
    public FragmentHabits() {
        // Required empty public constructor
    }

//    @SuppressLint("ValidFragment")
//    public FragmentHabits(RVAdapter adapter, LinearLayoutManager llm, String parameter3) {
//    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentHabits.
     */
    // TODO: Rename and change types and number of parameters
    //Стандартные конструкторы
    public static FragmentHabits newInstance(String param1, String param2) {
        FragmentHabits fragment = new FragmentHabits();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    //Конуструктор в который передаетя адаптер, а также мееджера лейаута. В нем происходит инициализация атрибутов
    public static FragmentHabits newInstance(RVAdapter rvadapter, LinearLayoutManager llm, String parameter3) {
        FragmentHabits fragment = new FragmentHabits();
        fragment.rvadapter = rvadapter;
        fragment.llm = llm;
        fragment.mParam1 = parameter3;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        dialogCounter = 0;
        silverDialogCounter = 0;
        goldDialogCounter = 0;

        //Хэндлер, который запускает диалог. Сделано для того чтобы диалоги не наслаивались друг на друга
            handler = new Handler() {
            public void handleMessage(android.os.Message msg)
            {
             if (msg.what==SettingsHelper.silverStar) { silverPostponedDialog.show(); }
             if (msg.what==SettingsHelper.goldStar)   { goldPostponedDialog.show(); }
            };
        };

    }

    //В методе происходит заполнение фрагмента и сооответствующего списка CardView данными
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Инициализация фрагмента. В лейауте фрагмента определен recycler view для отображения данных списком
        View v = inflater.inflate(R.layout.fragmenthabits, container, false);

        //Инициализация recycler view
        RecyclerView rv = (RecyclerView) v.findViewById(R.id.rv);

        //Считывание данных из таблицы с привычками и инициализация массива
        this.context = container.getContext();
        dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        habits = dbHelper.getData(db);
        dbHelper.close();

      /*  i = 0;
        long timeToSet = 0;

        do {
           //int id        = habits.get(i).getID();
           //String name   = habits.get(i).getName();
           int status     = habits.get(i).getStatus();
           long time      = habits.get(i).getTime();
           long timestamp = habits.get(i).getTimeStamp();

            long currentTime = (System.currentTimeMillis());

            if(status == SettingsHelper.statusNew || status == SettingsHelper.statusPaused)
            {
                timeToSet = time;
            }
            else if (status == SettingsHelper.statusActiveNonStop || status == SettingsHelper.statusActiveResumed)
            {
                timeToSet = time + (currentTime - timestamp) ;
            }

            if (timeToSet >= SettingsHelper.aimTime && status != SettingsHelper.statusCompletedGold)
             {
                 //nothing
             }

            i = i + 1;
        }
        while( i < habits.size() );
        habits = dbHelper.getData(db); */

        //Инициализация адаптера и лейаут менеджера
        LinearLayoutManager llm = new LinearLayoutManager(container.getContext());
        rvadapter = new RVAdapter(habits);

        //Передача адаптера и лейаута менеджера в recycle view (
           rv.setLayoutManager(llm); //Менеджер отвечает за расположение элементов друг относительно друга
           rv.setAdapter(rvadapter); //Наполнение
        // Inflate the layout for this fragment
        return v;
    }

    //Кусок стандартного кода
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    //Кусок стандартного кода
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
      }

      //Кусок стандартного кода
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //Кусок стандартного кода
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
      public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //В этом методе данные обновляются нал любое открытие экрана
    @Override
    public void onResume() {
        super.onResume();

        //Считываются настройки единиц отображения времени
        Boolean inHours = settingsHelper.getHoursSetting(context);
        if (inHours == true)
        {SettingsHelper.timeRate = SettingsHelper.timeRateinHours;} //display in Hours
        else
        {SettingsHelper.timeRate = SettingsHelper.timeRateinDays;} //display in Days

        //Считываются актуальные данные из таблицы БД
        if (context!=null) {
            dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
           habits = dbHelper.getData(db);


            i = 0;
           long timeToSet = 0;

            //Проверяем каждую привычку для определения необходимости отображения диалога с присвоением звезды
            do {
                int id        = habits.get(i).getID();
                String name   = habits.get(i).getName();
                int status    = habits.get(i).getStatus();
                long time      = habits.get(i).getTime();
                long timestamp = habits.get(i).getTimeStamp();

                long currentTime = System.currentTimeMillis();

                //Происходит расчет переменно timeToSet, которая используется для отображения актуального суммарного времени активности привычки
                //Если привычка новая или она запускалась и позже была остановлена
                if(status == SettingsHelper.statusNew || status == SettingsHelper.statusPaused)
                {
                    timeToSet = time;
                }
                //Если привычка активна или перезапущена, то прибавляем время которое прошло с момента аткивации
                else if (status == SettingsHelper.statusActiveNonStop || status == SettingsHelper.statusActiveResumed)
                {
                    timeToSet = time + (currentTime - timestamp) ;
                }

                //Если актуальное время больше цели, то показываем диалог со звездой
                if (timeToSet >= SettingsHelper.aimTime && status != SettingsHelper.statusCompletedGold)
                {

                        //Проверяем какую именно звезду надо присвоить

                        //Инициализируем переменную
                        int starAchieved = SettingsHelper.noStar;

                       //Определяем какую звезду присвоить вызывая API из класса CardDetails
                       //(эту API следовало бы вынести в отдельный класс)
                        CardDetails cardDetails = new CardDetails();
                        starAchieved = cardDetails.checkForStar(id, context);

                    //Показываем диалоги, если достигнута звезда. Считаем количество диалогов, чтобы не было их нагромождения
                        if (starAchieved == SettingsHelper.goldStar)
                        {
                            dialogCounter = dialogCounter + 1;
                            showGoldDialog(name, context, id);
                        }
                        else if (starAchieved == SettingsHelper.silverStar)
                        {
                            dialogCounter = dialogCounter + 1;
                            showSilverDialog(name, context, id);
                        }
                }

                i = i + 1;
            }
            while( i < habits.size() );

            //Обновляем экран актуальными данными
            rvadapter.update(habits);
        }
       }


    //Метод для отображения диалога
    public void showGoldDialog(String name, final Context context, final int ID)
    {
        CardDetails cardDetails = new CardDetails();
        long doneTime = SettingsHelper.aimTime;
        long timeToSetDoneTime =  SettingsHelper.aimTime / SettingsHelper.timeRate;

        //Определить единицы для диалога и их склонение (час, часов, день, дней, дня...)
        String unitDone = cardDetails.determineUnit(doneTime, SettingsHelper.timeRate, context);

        //Формирование окра диалога
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.congratulationsTitle));
        goldDialogCounter = goldDialogCounter  + 1;

        if (dbHelper.getGoldStarsNumber() == 0 &&  goldDialogCounter == 1)
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

        builder.setIcon(R.drawable.stargold);
        builder.setCancelable(false);

        //Обработка нажадния кнопки ОК. Золотая звезда будет присвоена только после диалога с пользователем
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DBHelper dbHelper;
                SQLiteDatabase db;
                dbHelper = new DBHelper(context);
                db = dbHelper.getWritableDatabase();
                dbHelper.updateStar(db, ID, SettingsHelper.goldStar);
                dbHelper.increaseGoldStarsNumber();

                dbHelper.updateHabitStatus(db, ID, SettingsHelper.statusCompletedGold);
                dbHelper.updateTimeTimeStamp(db, ID, 0, 0);
                dbHelper.updateHabitAlarmStatus(db, ID, SettingsHelper.alarmStatusNotActive);
                AlarmHelper alarmHelper = new AlarmHelper();
                alarmHelper.cancelNotification(context, ID);

                Activity activity;
                activity = getActivity();
                if ( (!(activity==null)) && (dialogCounter==1))
                {
                  activity.getFragmentManager().beginTransaction().replace(R.id.container, new FragmentHabits()).commit();
                }

                //Закрываем диалог и уменьшаем счетчик открытых диалогов
                dialogCounter = dialogCounter-1;
                dialog.dismiss();

            }
        });

        AlertDialog dialog = builder.create();


       //Отображать псследний диалог не сразу, а после паузы. Без этого падает дамп, т.к. какие то переменные почему то еще не инициализированы
        if (dbHelper.getGoldStarsNumber() == 0 &&  goldDialogCounter == 1)
        {
            goldPostponedDialog = dialog;
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                  try
                  {
                    Thread.sleep(101);
                      handler.sendEmptyMessage(SettingsHelper.goldStar);
                  }
                     catch (InterruptedException e)
                  {
                    e.printStackTrace();
                  }
                }
            }
            );
                t.start();
        }
        else {
            dialog.show();
        }
    }

    //Метод для отображения серебрянного диалога. Отличается от золотого тем, что нужно показать сколько времени осталось
    //до золотой звезды. И предложить выбор или оставить серебряную или продложить последнюю неприрывную сессию за золотой звездой
    void showSilverDialog(String name, final Context context, final int ID)
    {
        long currentTime = System.currentTimeMillis();
        CardDetails cardDetails = new CardDetails();

        long doneTime = SettingsHelper.aimTime;
        long timeToSetDoneTime =  SettingsHelper.aimTime / SettingsHelper.timeRate;
        String unitDone = cardDetails.determineUnit(doneTime, SettingsHelper.timeRate, context);

        //Последняя сессия была непрерывной. Расчитываем сколько неприрывной работы нужно до золотой звезды
        long leftTime = SettingsHelper.timeRate + SettingsHelper.aimTime - (currentTime - habits.get(i).getTimeStamp() );
        long timeToSetLeftToGold =  leftTime / SettingsHelper.timeRate;
        String unitToSetLeft = cardDetails.determineUnit(leftTime, SettingsHelper.timeRate, context);

        long unbrokenTime = doneTime - leftTime;
        long timeToSetUnbrokenTime =  unbrokenTime / SettingsHelper.timeRate;
        String unitUnbrokenTime = cardDetails.determineUnit(unbrokenTime, SettingsHelper.timeRate, context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.congratulationsTitle));

        silverDialogCounter = silverDialogCounter + 1;
        if (dbHelper.getSilverStarsNumber() == 0 && silverDialogCounter == 1)
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
        else //Серебряная звездочка уже не первая
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

        //Продолжаем работу над привычкой до золотой звезды (и непрервыное время будет зачислено в текущее время привычки)
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DBHelper dbHelper;
                SQLiteDatabase db;
                dbHelper = new DBHelper(context);
                db = dbHelper.getWritableDatabase();
                Habits habit = dbHelper.getRecord(db, ID);

                long currentTime = System.currentTimeMillis();
                long unbrokenTime = currentTime - habit.getTimeStamp() ;

                dbHelper.updateStar(db, ID, SettingsHelper.silverStar);
                dbHelper.increaseSilverStarsNumber();

                dbHelper.updateHabitStatus(db, ID, SettingsHelper.statusActiveNonStop);
                dbHelper.updateTimeTimeStamp(db, ID, 0, currentTime-unbrokenTime );

                Activity activity;
                activity = getActivity();
                if ( (!(activity==null)) && (dialogCounter==1))
                {
                  activity.getFragmentManager().beginTransaction().replace(R.id.container, new FragmentHabits()).commit();
                }
                dialogCounter = dialogCounter-1;
                dialog.dismiss();

            }
        });

        //Не продолжаем работу. Оставляем серебряную звезду и делаем привычку не активной
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DBHelper dbHelper;
                SQLiteDatabase db;
                dbHelper = new DBHelper(context);
                db = dbHelper.getWritableDatabase();

                dbHelper.updateStar(db, ID, SettingsHelper.silverStar);
                dbHelper.increaseSilverStarsNumber();

                dbHelper.updateHabitStatus(db, ID, SettingsHelper.statusCompletedSilver);
                dbHelper.updateTimeTimeStamp(db, ID, 0, 0);
                dbHelper.updateHabitAlarmStatus(db, ID, SettingsHelper.alarmStatusNotActive);

                AlarmHelper alarmHelper = new AlarmHelper();
                alarmHelper.cancelNotification(context, ID);

                Activity activity;
                activity = getActivity();
                if ( (!(activity==null)) && (dialogCounter==1))
                {
                  activity.getFragmentManager().beginTransaction().replace(R.id.container, new FragmentHabits()).commit();
                }
                dialogCounter = dialogCounter-1;
                dialog.dismiss();
            }
        });

        //Отображать псследний диалог не сразу, а после паузы. Без этого падает дамп, т.к. какие то переменные почему то еще не инициализированы
        AlertDialog dialog = builder.create();
        if (dbHelper.getSilverStarsNumber() == 0 && silverDialogCounter == 1)
        {
            silverPostponedDialog = dialog;
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Thread.sleep(102);
                        handler.sendEmptyMessage(SettingsHelper.silverStar);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            );
            t.start();
        }
        else {
            dialog.show();
        }

    }


}
