package pt.personaltrainer;
//Главный класс - служит для управления фрагментами и перехода к экрану с деталями привычек

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Random;

import pt.personaltrainer.fragments.FragmentCalculator;
import pt.personaltrainer.fragments.FragmentHabits;
import pt.personaltrainer.fragments.FragmentInfo;
import pt.personaltrainer.fragments.FragmentProgress;
import pt.personaltrainer.fragments.FragmentSettings;


public class MainActivity extends       AppCompatActivity
                          implements    NavigationView.OnNavigationItemSelectedListener,
                                        FragmentInfo.OnFragmentInteractionListener,
                                        FragmentHabits.OnFragmentInteractionListener,
                                        FragmentSettings.OnFragmentInteractionListener {

    public Context context;

    FragmentSettings    fsettings;
    FragmentHabits      fhabits;
    FragmentProgress    fprogress;
    FragmentCalculator  fcalculator;
    FragmentInfo        finfo;
    FragmentTransaction ftrans;

    TextView tvRunningText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Поддержка тулбара
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Поддержка бокового меню
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            public void onDrawerOpened(View drawerView) //Вызов бокового меню
            {
                //При каждом вызвое бокового меню генерируется текстовое сообщение из файла со строками
                //TODO: переделать в виде матрицы без кейса
                Random randomGenerator = new Random();
                int randIndex = randomGenerator.nextInt(10);
                String motivation = "" ;

                switch(randIndex)
                {
                    case 0:
                        motivation = getResources().getString(R.string.short_motivation_00);
                        break;
                    case 1:
                        motivation = getResources().getString(R.string.short_motivation_01);
                        break;
                    case 2:
                        motivation = getResources().getString(R.string.short_motivation_02);
                        break;
                    case 3:
                        motivation = getResources().getString(R.string.short_motivation_03);
                        break;
                    case 4:
                        motivation = getResources().getString(R.string.short_motivation_04);
                        break;
                    case 5:
                        motivation = getResources().getString(R.string.short_motivation_05);
                        break;
                    case 6:
                        motivation = getResources().getString(R.string.short_motivation_06);
                        break;
                    case 7:
                        motivation = getResources().getString(R.string.short_motivation_07);
                        break;
                    case 8:
                        motivation = getResources().getString(R.string.short_motivation_08);
                        break;
                    case 9:
                        motivation = getResources().getString(R.string.short_motivation_09);
                        break;
                    case 10:
                        motivation = getResources().getString(R.string.short_motivation_10);
                        break;
                  }

                  //Добавляем текст к вьюхе
                        tvRunningText = (TextView) findViewById(R.id.tvRuningTextSlider);
                        tvRunningText.setSelected(true);
                        tvRunningText.setText(motivation);
                        tvRunningText.setVisibility(View.VISIBLE);
            }
            public void onDrawerClosed(View drawerView)
            {
                tvRunningText = (TextView) findViewById(R.id.tvRuningTextSlider);
                tvRunningText.setVisibility(View.INVISIBLE);}
        }  ;

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Формирование бокового меню из nav_view (activity_main.xml)
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        //По умолчанию выставляем активным главный пункт меню (экран с привычками)
        navigationView.setCheckedItem(R.id.nav_habits);
        navigationView.setNavigationItemSelectedListener(this);

        //Инициализируем фрагменты
        fsettings    =  new FragmentSettings();
        fprogress    =  new FragmentProgress();
        fcalculator  =  new FragmentCalculator() ;
        fhabits      =  new FragmentHabits();
        finfo        =  new FragmentInfo();

        ftrans = getFragmentManager().beginTransaction();

        //Выставляем надпись на тулбаре
        toolbar.setTitle(R.string.lable);
        setTitle(R.string.lable);

        //По умолчанию экран откроется с фрагментом со списком привычек
        ftrans.replace(R.id.container, fhabits);

        ftrans.commit();
    }


    //Обработка нажатия кнопки назад
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }

        if (fhabits != null && fhabits.isVisible())
        {
            super.onBackPressed();
        }
        else
            {
                ftrans = getFragmentManager().beginTransaction();
                ftrans.replace(R.id.container, fhabits);
                ftrans.commit();
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                toolbar.setTitle(R.string.lable);
                NavigationView navView= (NavigationView) findViewById(R.id.nav_view);
                navView.setCheckedItem(R.id.nav_habits);
        }
    }

    //Создание меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Обработка нажатия "информационной" кнопки в правом верхнем пункте меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         int id = item.getItemId();

        //Делаем "нажатым" выбранный пункт меню
       NavigationView navView= (NavigationView) findViewById(R.id.nav_view);
       navView.setCheckedItem(R.id.nav_info);

       // Заменяем текущий фрагмент фрагментов "О программе"
        if (id == R.id.action_settings) {
        ftrans = getFragmentManager().beginTransaction();
        ftrans.replace(R.id.container, finfo);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.labelInfo);
        ftrans.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //обработка нажатия пункта меню - переход к другому фрагменту
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(false);
        // Handle navigation view item clicks here.
        int id = item.getItemId();

       ftrans = getFragmentManager().beginTransaction();

        if (id == R.id.nav_settings) {
            ftrans.replace(R.id.container, fsettings);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.lableSettings);


        } else if (id == R.id.nav_habits) {

            ftrans.replace(R.id.container, fhabits);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.lable);

        } else if (id == R.id.nav_info) {
            ftrans.replace(R.id.container, finfo);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.labelInfo);
        }
        ftrans.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Обработка нажатия на карточку
    public void onCardClick(View v){
        TextView  habitView;
        ImageView habitImage;

        String name;
        String description;
        String habitDays;
        int ID;
        int habitPhotoID;

        //Определяем какая именно карточка нажата, считываем ее данные
        habitView = (TextView) v.findViewById(R.id.habit_id);
        ID = (int) habitView.getTag();

        habitView = (TextView) v.findViewById(R.id.habit_name);
        name = habitView.getText().toString();

        habitView = (TextView) v.findViewById(R.id.habit_description);
        description = habitView.getText().toString();

        habitView = (TextView) v.findViewById(R.id.habit_days);
        habitDays = habitView.getText().toString();

        habitImage = (ImageView) v.findViewById(R.id.habit_photo);
        habitPhotoID = (Integer) habitImage.getTag();

        //Формируем интент для перехода к новому Activity (экрану)
        Intent intent = new Intent(this, CardDetails.class);

        //Передаем данные в этот инетнт
        intent.setAction(getString(R.string.goToCardAction));
        intent.putExtra("hname", name);
        intent.putExtra("hdescription", description);
        intent.putExtra("hphotoid", habitPhotoID);
        intent.putExtra("hdays", habitDays);
        intent.putExtra("ID", ID);

        //Запускаем переход
        startActivity(intent);
    }

    public static Context getContext()
    {
         return MainActivity.getContext();
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }
}

