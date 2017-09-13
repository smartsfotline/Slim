package pt.personaltrainer.fragments;
/*Фрагмент для формирования раздела 'О Программе'*/


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.style.TextAlignment;


import pt.personaltrainer.CardDetails;
import pt.personaltrainer.MyBullet;
import pt.personaltrainer.R;

//Кусок стандартного кода
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentInfo.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentInfo extends Fragment implements View.OnClickListener  {

    //Кусок стандартного кода
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
       void onFragmentInteraction(Uri uri);
    }

    //Кусок стандартного кода
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //Кусок стандартного кода
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Контекст
    private Context context;
    private OnFragmentInteractionListener mListener;

    //Обработчик для задержки загрузки
    Handler handler;

    //Вьюхи стандартные
    View v;
    TextView tvInfoHeader;
    TextView tvTimeTo;
    TextView tvGoodWishes;
    TextView tvGoodWishes2;

    //Кастомные вьюхи, подключенные из пакета package com.bluejamesbond.text через настройки
    //Служат для отображения текста с выравниванием по ширине
    DocumentView docInfoContent;
    DocumentView docInfoList;

    Button btStartSlim;

    //Кусок стандартного кода
    public FragmentInfo() {
        // Required empty public constructor
    }

    //Кусок стандартного кода
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentInfo newInstance(String param1, String param2) {
        FragmentInfo fragment = new FragmentInfo();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Кусок стандартного кода
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //Хендлер обрабатывае месседж. Если параметр месседжа равен 200, то все блоки становятся видимыми
        //Такая логика нужна для более гладкого и аккуратного отображения больших объемов текста при загрузке страницы
        handler = new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                if (msg.what==200)
                {
                    btStartSlim.setVisibility(View.GONE);

                    tvInfoHeader.setVisibility(View.VISIBLE);
                    tvTimeTo.setVisibility(View.VISIBLE);;
                    tvGoodWishes.setVisibility(View.VISIBLE);
                    tvGoodWishes2.setVisibility(View.VISIBLE);

                    docInfoContent.setVisibility(View.VISIBLE);;
                    docInfoList.setVisibility(View.VISIBLE);;
                }
            };
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Инициализация вьюх данными при их создании
        context = container.getContext();
        v = (View) inflater.inflate(R.layout.fragment_info, container, false);

        docInfoContent = (DocumentView) v.findViewById(R.id.tvInfoContent) ;
        docInfoContent.getDocumentLayoutParams().setTextAlignment(TextAlignment.JUSTIFIED);
        //Получаем текст из файла со сторками
        docInfoContent.setText(getString(R.string.infoDescription));

        //Формирование данных списком, для этого используется класс MyBullet
        SpannableStringBuilder totalSpan = new SpannableStringBuilder();
        MyBullet bulletSpan;
        Spannable span;
        //Текст загоняется в строку
        String infoList = getString(R.string.infoList);
        //Строка разбивается на массив строк. Разбиение происходит по символам новой строки
        String[] partsOptional = infoList.split("\n");

        //Каждый из элементов массива строк добавляется в список. Элемент списка начинается с синего кружка в начале (bullet)
        for (String string: partsOptional)
        {
            bulletSpan = new MyBullet(0,getResources().getColor(R.color.pickerblue));
            span = new SpannableString(string);
            span.setSpan(new CardDetails.MySpan(20), 0, string.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            span.setSpan(bulletSpan, 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            totalSpan.append(span);
            totalSpan.append("\n");
        }

        //Передача списка в DocumentView
        docInfoList = (DocumentView) v.findViewById(R.id.tvInfoList) ;
        docInfoList.getDocumentLayoutParams().setTextAlignment(TextAlignment.JUSTIFIED);
        docInfoList.setText(totalSpan);

        tvInfoHeader = (TextView) v.findViewById(R.id.tvInfoHeader);
        tvTimeTo = (TextView) v.findViewById(R.id.tvTimeTo);
        tvGoodWishes = (TextView) v.findViewById(R.id.tvGoodWishes);
        tvGoodWishes2 = (TextView) v.findViewById(R.id.tvGoodWishes2);


 //Кусок логики, который делал ссылки кликабельными. Отпала необходимость

/* TextView textView = (TextView) v.findViewById(R.id.info_link);
    //String textWithLink = SettingsHelper.urlToSportWiki; // "<a href=\"http://www.sportwiki.to\">Подробнее</a>";
    String textWithLink = SettingsHelper.urlTo21day;
    textView.setText(Html.fromHtml(textWithLink, null, null));
    textView.setLinksClickable(true);
    textView.setMovementMethod(LinkMovementMethod.getInstance());

    CharSequence text = textView.getText();
    if (text instanceof Spannable)
       {
         textView.setText(MakeLinksClicable.reformatText(text));
       }     */

      btStartSlim = (Button) v.findViewById(R.id.btStartSlim);
      btStartSlim.setOnClickListener(this);

        //Создание нового потока, который задержит выполнение потока на 200 милисекунд, а потом пошлет в OnCreate message
        //Такой подход необходим для аккуратного отображения больших объемов текста
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
        t.start(); //Запуск нового потока
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

    //Метод обрабытвал кнопку, по которой осуществлялся переход на главный экран (к главному фрагменту FragmentHabits)
    //В настоящее время кнопки нет
    @Override
    public void onClick(View v) {

        Activity activity;
        activity = getActivity();
        if ( !(activity==null) )
        {
            activity.getFragmentManager().beginTransaction().replace(R.id.container, new FragmentHabits()).commit();

        NavigationView navView= (NavigationView) activity.findViewById(R.id.nav_view);
        navView.setCheckedItem(R.id.nav_habits);

            Toolbar toolbar = (Toolbar)  activity.findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.lable);
        }
    }
}


