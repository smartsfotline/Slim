package pt.personaltrainer;
//Класс, формирующий карточку привычки в списке привычек

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by aliaksandr.shakavets on 17-Apr-17.
 */

//Адаптер для recycler view
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.HabitViewHolder>{

//View holder for recycler view
public static class HabitViewHolder extends RecyclerView.ViewHolder{
  //Объявление переменных для вьюх
    CardView cv;
    TextView habitName;
    TextView habitDescription;
    TextView habitState;
    TextView habitStar;
    TextView habitDays;
    TextView habitID;
    ImageView habitPhoto;
    ImageView ivBigStar;

    //Конструктор
    HabitViewHolder(View itemView){
        super(itemView);

        //Инициализация вьюх
        cv               = (CardView)  itemView.findViewById(R.id.cv);
        habitName        = (TextView)  itemView.findViewById(R.id.habit_name);
        habitDescription = (TextView)  itemView.findViewById(R.id.habit_description);
        habitPhoto       = (ImageView) itemView.findViewById(R.id.habit_photo);
        habitDays        = (TextView)  itemView.findViewById(R.id.habit_days);
        habitState       = (TextView)  itemView.findViewById(R.id.habit_state);
        habitStar        = (TextView)  itemView.findViewById(R.id.habit_star);
        habitID          = (TextView)  itemView.findViewById(R.id.habit_id);
        ivBigStar        = (ImageView) itemView.findViewById(R.id.iv_big_star);
    }
}

    //Список привыычек
    private  List<Habits> habits;

    //Конструктор, который используется для передачи в адаптер списка привычек (инициализирует атрибут)
    public RVAdapter(List<Habits> habits){
        this.habits = habits;
    }

    //Метод возввращает количество записей в адаптере
    @Override
    public int getItemCount(){
        return habits.size();
    }

    //Создается вью холдер (новая запись в списке) - обязательный стандартрый метод
    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup viewGroup, int i){
        //С помощью инфлятора на основе item.xml создается вьюха
       View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);

        //На основе созданной вьюхи создаетсся вью холдер (в конструкторе происходит инициализация)
        HabitViewHolder hvh = new HabitViewHolder(v);
        return hvh;
    }

    //Главный метод
    //Каждый Вью холдер связывается с главной вьюхой (вызывается столько раз - сколько записей в списке)
    //В него также передается порядковый номер вью холдера. С помощью этого номера определеяется запись из массива
    //Каждый элемент из CardView (item.xml) заполняется соответствующими данными из записи массива
        @Override
    public void onBindViewHolder(HabitViewHolder habitViewHolder, int i){

        //ID не прописывается в элемент (т.к. не отображается), а прикрепляется к нему
        habitViewHolder.habitID.setTag(habits.get(i).ID);

        //Данные заполняются из строки массива
        habitViewHolder.habitName.setText(habits.get(i).name);
        habitViewHolder.habitDescription.setText(habits.get(i).description);
        habitViewHolder.ivBigStar.setBackgroundResource(0);

        int status = habits.get(i).status;
        int star = habits.get(i).star;
        long timeToSet = 0;


        //Small icons setting
       //по умолчанию без рисунков
       habitViewHolder.habitStar.setBackgroundResource(0);

       //В зависимости от статуса выставляется символ
        if (status == SettingsHelper.statusNew)
        {habitViewHolder.habitState.setBackgroundResource(0);}
        else if (status == SettingsHelper.statusPaused)
        {habitViewHolder.habitState.setBackgroundResource(R.drawable.icon_paused);}
        else if (status == SettingsHelper.statusActiveResumed)
        {habitViewHolder.habitState.setBackgroundResource(R.drawable.icon_resumed);}
        else if (status == SettingsHelper.statusActiveNonStop)
        {habitViewHolder.habitState.setBackgroundResource(R.drawable.icon_play);
          if (star == SettingsHelper.silverStar) {habitViewHolder.habitStar.setBackgroundResource(R.drawable.starsilver);}}
        else if (status == SettingsHelper.statusCompletedGold)
        {
         habitViewHolder.habitState.setBackgroundResource(0);
         habitViewHolder.habitStar.setBackgroundResource(0);
        }

       //Цвет текста меняется в зависимости от статуса, также меняется изображение звезды
       //Text color, time settings, big Stars
        if (status == SettingsHelper.statusNew ||status == SettingsHelper.statusPaused ) //new
        {
            habitViewHolder.habitName.setTextColor(Color.rgb(30,50,30));
            habitViewHolder.habitDays.setTextColor(Color.rgb(30,50,30)); //grey
            timeToSet = (habits.get(i).time);
            habitViewHolder.ivBigStar.setBackgroundResource(0);

        } //Black
            else if (status == SettingsHelper.statusActiveNonStop || status == SettingsHelper.statusActiveResumed) //active
            {
                habitViewHolder.habitName.setTextColor(Color.rgb(79, 193, 84));
                habitViewHolder.habitDays.setTextColor(Color.rgb(79, 193, 84));
                 long currentTime = (System.currentTimeMillis());
                timeToSet = (habits.get(i).time) + (currentTime - (habits.get(i).timestamp)) ;
                habitViewHolder.ivBigStar.setBackgroundResource(0);
             } //Green

        else if (status == SettingsHelper.statusCompletedGold) //completed
        {
            habitViewHolder.habitName.setTextColor(Color.rgb(255,215,0));  //yellow
            habitViewHolder.ivBigStar.setBackgroundResource(R.drawable.stargold);
             timeToSet = habits.get(i).time;
        }

        else if (status == SettingsHelper.statusCompletedSilver) //completed
        {
            habitViewHolder.habitName.setTextColor(Color.rgb(30,50,30));  //yellow
            habitViewHolder.ivBigStar.setBackgroundResource(R.drawable.starsilver);
            timeToSet = habits.get(i).time;
        } //Green

        if (timeToSet >= SettingsHelper.aimTime)   {timeToSet = SettingsHelper.aimTime-SettingsHelper.timeRate;}
        String strMinutes = String.valueOf(timeToSet/SettingsHelper.timeRate);


       if (status == SettingsHelper.statusCompletedGold || status == SettingsHelper.statusCompletedSilver)
            {
                float size = (float) 1;
                habitViewHolder.habitDays.setText("");
                habitViewHolder.habitDays.setTextScaleX(size);

            }
       else
           {
               habitViewHolder.habitDays.setText(strMinutes);
           }

        habitViewHolder.habitPhoto.setImageResource(habits.get(i).photoId);
        habitViewHolder.habitPhoto.setTag(habits.get(i).photoId);
    }

//Метод обновляет массив новыми данными
    public void update(List<Habits> habits_new){
        this.habits.clear();
        this.habits.addAll(habits_new);
        notifyDataSetChanged();
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }
}

