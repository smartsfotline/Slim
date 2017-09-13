package pt.personaltrainer.fragments;
/*Фрагмент используются для работы с экраном "Settings" (Настройки)
  По умолчанию используется стандартная болванка для фрагментов*/

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.support.v7.widget.SwitchCompat;
import android.widget.Toast;
import pt.personaltrainer.AlarmHelper;
import pt.personaltrainer.R;
import pt.personaltrainer.SettingsHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentSettings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
//Главный класс, реализующи логику фрагмента
public class FragmentSettings extends Fragment {
    //Параметры из стандартной болванки
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //Кусок стандартной болванки
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public Context context;

    //Обработчик события (listener) для взаимодействия с другими фрагментами
    private OnFragmentInteractionListener mListener;

    //Объявление объектов типа "переключатель"
    SwitchCompat stSwitch = null;
    SwitchCompat stSwitchSound = null;

    View v;

    //Инициализация хелпера для настроект
    SettingsHelper settingsHelper = new SettingsHelper();


    //Кусок стандартной болванки
    public FragmentSettings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentSettings.
     */
    // TODO: Rename and change types and number of parameters

    //Конструктор класса, принимает на вход два строковых параметра и записывает их в атрибуты родительского класса
    public static FragmentSettings newInstance(String param1, String param2) {
        FragmentSettings fragment = new FragmentSettings();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    //Кусок стандартной болванки - инициализирует атрибуты
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

   //Стандартный метод, в котором, однако, реализована вся необходимая логика фрагмента
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        //Инициализация вьюхи, отображающей фрагмент. Передается id лейаута
       v = (View) inflater.inflate(R.layout.fragment_settings, container, false);

        //Иниациализация контекста для вьюхи
      this.context = container.getContext();

        //Иницилизация переключателей на экране (уведомления, звук)
        stSwitch      = (SwitchCompat) v.findViewById(R.id.swtchNotification);
        stSwitchSound = (SwitchCompat) v.findViewById(R.id.swtchSound);

        //Считываем значение настройки из таблицы с настройками в БД (для работы с БД и передаем контекст)
        stSwitch.setChecked(settingsHelper.getNotificationSetting(context));

        //Устанавливаем обработчик настройки нотификаций
        stSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                AlarmHelper alarmHelper = new AlarmHelper();


                if (bChecked) { //Уведомления Разрешены
                    settingsHelper.setNotificationSetting(true, context); //Выставляем настройку в БД
                    stSwitchSound.setEnabled(true); //Делаем настройку звука уведомлений доступной
                    stSwitchSound.setChecked(true); //Включаем звук по умолчанию
                    settingsHelper.setSoundSetting(false,context);

                    Toast.makeText(context, R.string.notificationsAllowed, Toast.LENGTH_SHORT).show();

                } else {
                    settingsHelper.setNotificationSetting(false, context); //Выставляем настройку в БД
                    alarmHelper.cancelAllNotifications(context);           //Удаляем все уведомления с помощью API из хелпера, рейзим месседж оттуда
                    stSwitchSound.setEnabled(false);                       //Дисейблим настройку звука
                    stSwitchSound.setChecked(false);                       //Выставляем звук в ВЫКЛ
                    settingsHelper.setSoundSetting(false, context);        //Выставляем настройку звука ВЫКЛ в БД

                }
            }
        });

        //Настройка звука
        stSwitchSound.setEnabled(settingsHelper.getNotificationSetting(context));
        stSwitchSound.setChecked(settingsHelper.getSoundSetting(context));
        stSwitchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {

                if (bChecked) {
                    settingsHelper.setSoundSetting(true, context);
                    Toast.makeText(context, R.string.soundOn, Toast.LENGTH_SHORT).show();
                } else {
                    settingsHelper.setSoundSetting(false, context);
                    Toast.makeText(context, R.string.soundOff, Toast.LENGTH_SHORT).show();
                }
                //Инициализация хелпера уведомлений
                AlarmHelper alarmHelper = new AlarmHelper();
                //Метод, который считывает в БД все активные привычки и перезапускает все уведомления уже с новми настройками
                  alarmHelper.updateAllActiveNotifications(context);
            }
        });

        //Включение или выключение заставки при загрузке
        stSwitch = (SwitchCompat) v.findViewById(R.id.swtchLogo);
        stSwitch.setChecked(settingsHelper.getLogoSetting(context));
        stSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    settingsHelper.setLogoSetting(true, context);
                    Toast.makeText(context, R.string.logoOn, Toast.LENGTH_SHORT).show();
                } else {
                    settingsHelper.setLogoSetting(false, context);
                    Toast.makeText(context, R.string.logoOff, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Отображение прогресса в часах или днях
        stSwitch = (SwitchCompat) v.findViewById(R.id.swtchHours);
        stSwitch.setChecked(settingsHelper.getHoursSetting(context));
        stSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    settingsHelper.setHoursSetting(true, context);
                    Toast.makeText(context, R.string.timeInHoursSet, Toast.LENGTH_SHORT).show();
                } else {
                    settingsHelper.setHoursSetting(false, context);
                    Toast.makeText(context, R.string.timeInDefaultUnit, Toast.LENGTH_SHORT).show();
                }
            }
        });

       return v;
    }

    //Кусок стандартной болванки
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    //Кусок стандартной болванки
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

    //Кусок стандартной болванки
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //Кусок стандартной болванки
      public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
