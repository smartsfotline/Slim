package pt.personaltrainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aliaksandr.shakavets on 21-Apr-17.
 */

public class DBHelper extends SQLiteOpenHelper {
    final String LOG_TAG = "myLogs";
    public Context context;

    //Атрибуты класса
    private static int activeHabitsNumber = 0; //количество активных привычек
    private static int silverStarsNumber = 0;  //количество серебряных привычек
    private static int goldStarsNumber = 0;    //количество золотых привычек

    //Класс конструктор, создает новую БД только если БД с таким именем не существует
    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "myDB501", null, 1);
        this.context = context;
    }

    //При создании БД создаются новые таблицы dbhabits и таблица dbsettings
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Log.d(LOG_TAG, "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL("create table dbhabits ("
                + "id integer primary key autoincrement," //id
                + "name text,"                            //текст привычки
                + "description text,"                     //описание првычки
                + "obligatory text,"                      //обязательные к выполнению советы
                + "optional text,"                        //рекомендательные советы
                + "knowledge text,"                       //важно знать о привычке
                + "photoid integer,"                      //id картинки привычки
                + "status integer,"                       //статус привычки (new, active, ...)
                + "alarmstatus integer,"                  //статус уведомления приосаненного к привычке (not set, active, ...)
                + "alarmhours integer,"                   //часы уведомления
                + "alarmminutes integer,"                 //минуты уведомления
                + "time integer,"                         //время, которое привычка активна (микросекунд)
                + "timestamp integer,"                    //время, когда привычка стала актива
                + "star integer"                          //звезда, отображаемая для пользователя
                + ");");

       // создаем таблицу с настройками
        db.execSQL("create table dbsettings ("
                + "id integer primary key,"  //id настройки
                + "value integer"            //значение
                + ");");

        //Заполняем созданную таблицу данными из файла values/string.xml
        InitializeTable(db);

        //Заполняем таблицу с настройками значениями по умолчанию
        InitializeSettings(db);

        //Выставляем флаг о том, что это первый вызов
        SettingsHelper.isFirstCall = true;
    }

    //Метод, вызываемый при обновлении БД. Пустой - не заимплеменчен.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion){        }
    }

    //Метод возвращает количество привычек со статусом Active
    public int getActiveHabitsNumber(){
        return activeHabitsNumber;
    }

    //Метод возвращает количество привычек с серебряной звездой
    public int getSilverStarsNumber(){
        return silverStarsNumber;
    }

    //Метод возвращает количество привычек с золотой звездой
    public int getGoldStarsNumber(){
        return goldStarsNumber;
    }

    // Метод увеличивает количество активных привычек
    public void increaseActiveHabitsNumber(){ activeHabitsNumber = activeHabitsNumber + 1;  }

    //Метод увеличивает количество серебряных звезд
    public void increaseSilverStarsNumber(){
        silverStarsNumber = silverStarsNumber + 1;
    }

    //Метод увеличивает количество золотых звезд
    public void increaseGoldStarsNumber(){
        goldStarsNumber = goldStarsNumber + 1;
    }


    //Метод уменьшает количество активных привычек
    public void decreaseActiveHabitsNumber(){  activeHabitsNumber = activeHabitsNumber - 1;  }

    //Метод уменьшает количество серебряных звезд
    public void decreaseSilverStarsNumber(){
        silverStarsNumber = silverStarsNumber - 1;
    }

    //Метод уменьшает количество золотых звезд
    public void decreaseGoldStarsNumber(){
        goldStarsNumber = goldStarsNumber - 1;
    }




    //Метод возвращает массив с привычками из таблицы dbhabits
    public List<Habits> getData(SQLiteDatabase db){

     //Создаем массив привычек для его заполнения из таблицы
    List<Habits> habits = new ArrayList<>();

     //Задаем строку сортировки - так привычки будут отсортированы в курсоре, далее в массиве, и далее - на экране пользователя
    String orderBy = "status DESC, star DESC, time DESC, timestamp ASC";

    // делаем запрос всех данных из таблицы dbhabits, получаем Cursor (контейнер с данными)
    // сортировка вывборки задана в строке 'orderBy'
    Cursor c = db.query("dbhabits", null, null, null, null, null, orderBy);

    // ставим позицию курсора на первую строку выборки
    // если в выборке нет строк, вернется false, если  строки есть, то они добавляются в массив
    if (c.moveToFirst()) {

        // определяем номера столбцов по имени в выборке (находим столбец по имени)
        int idColIndex = c.getColumnIndex("id");
        int nameColIndex = c.getColumnIndex("name");
        int descriptionColIndex = c.getColumnIndex("description");

        int obligatoryColIndex = c.getColumnIndex("obligatory");
        int optionalColIndex = c.getColumnIndex("optional");
        int knowledgeColIndex = c.getColumnIndex("knowledge");

        int photoIDColIndex = c.getColumnIndex("photoid");
        int StatusColIndex = c.getColumnIndex("status");
        int alarmstatusColIndex= c.getColumnIndex("alarmstatus");
        int alarmHoursColIndex = c.getColumnIndex("alarmhours");
        int alarmMinutesColIndex = c.getColumnIndex("alarmminutes");
        int TimeColIndex = c.getColumnIndex("time");
        int TimeStampIDColIndex = c.getColumnIndex("timestamp");
        int starColIndex = c.getColumnIndex("star");

        //обнуляем атрибуты т.к. они будут актуализированы ниже
        //(для того чтобы данные были актуальны на момент обновления позльзователем экрана)
        activeHabitsNumber = 0;
        silverStarsNumber  = 0;
        goldStarsNumber    = 0;


        do {  //построчно считываем курсор (контейнер) в массив

            //Создаем новую строку массива и далее ее заполняем
            Habits habit = new Habits();

            habit.ID = c.getInt(idColIndex);
            habit.name = c.getString(nameColIndex);
            habit.description = c.getString(descriptionColIndex);
            habit.obligatory = c.getString(obligatoryColIndex);
            habit.optional   = c.getString(optionalColIndex);
            habit.knowledge  = c.getString(knowledgeColIndex);
            habit.photoId = c.getInt(photoIDColIndex);

            habit.status = c.getInt(StatusColIndex);
            //Увеличиваем количество активных привычек
            if (habit.status == SettingsHelper.statusActiveNonStop || habit.status == SettingsHelper.statusActiveResumed )
            {  increaseActiveHabitsNumber();  }

            habit.alarmstatus = c.getInt(alarmstatusColIndex);
            habit.alarmhours = c.getInt(alarmHoursColIndex);
            habit.alarmminutes = c.getInt(alarmMinutesColIndex);
            habit.time = c.getLong(TimeColIndex);
            habit.timestamp = c.getLong(TimeStampIDColIndex);

            habit.star = c.getInt(starColIndex);
            //Увеличиывает количество серебряных и золотых звезд
            if (habit.star == SettingsHelper.silverStar)
            {  increaseSilverStarsNumber();  }
            else if (habit.star == SettingsHelper.goldStar)
            {  increaseGoldStarsNumber();  }

            habits.add(habit);
        } while (c.moveToNext()); //до последней строки
    } else
        Log.d(LOG_TAG, "0 rows");
    c.close();

    return habits; //заполненный массив с данными
}


//Метод аналогичен методу getData, однако возвращает только те записи, для которых установлена нотификация (alarmStatus = 1)
//Метод необходим для обновления нотификаций после перезагрузки телефеона
    public List<Habits> getRecordsWithNoticiations(SQLiteDatabase db){
        List<Habits> alarmHabits = new ArrayList<>();

        String selection = "alarmstatus = ?"; // records where alarm set
        String alarmedStatus = "1";
        String[] selectionArgs = new String[] { alarmedStatus };

        // делаем запрос всех данных из таблицы myhabits, получаем Cursor
        Cursor c = db.query("dbhabits", null, selection, selectionArgs, null, null, null);

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");
            int descriptionColIndex = c.getColumnIndex("description");
//            int contentColIndex = c.getColumnIndex("content");
            int obligatoryColIndex = c.getColumnIndex("obligatory");
            int optionalColIndex = c.getColumnIndex("optional");
            int knowledgeColIndex = c.getColumnIndex("knowledge");

            int photoIDColIndex = c.getColumnIndex("photoid");
            int StatusColIndex = c.getColumnIndex("status");
            int alarmstatusColIndex= c.getColumnIndex("alarmstatus");
            int alarmHoursColIndex = c.getColumnIndex("alarmhours");
            int alarmMinutesColIndex = c.getColumnIndex("alarmminutes");
            int TimeColIndex = c.getColumnIndex("time");
            int TimeStampIDColIndex = c.getColumnIndex("timestamp");
            int starColIndex = c.getColumnIndex("star");


            do {
                Habits alarmHabit = new Habits();
                alarmHabit.ID = c.getInt(idColIndex);
                alarmHabit.name = c.getString(nameColIndex);
                alarmHabit.description = c.getString(descriptionColIndex);

                alarmHabit.obligatory = c.getString(obligatoryColIndex);
                alarmHabit.optional = c.getString(optionalColIndex);
                alarmHabit.knowledge = c.getString(knowledgeColIndex);

                alarmHabit.photoId = c.getInt(photoIDColIndex);
                alarmHabit.status = c.getInt(StatusColIndex);
                alarmHabit.alarmstatus = c.getInt(alarmstatusColIndex);
                alarmHabit.alarmhours = c.getInt(alarmHoursColIndex);
                alarmHabit.alarmminutes = c.getInt(alarmMinutesColIndex);
                alarmHabit.time = c.getLong(TimeColIndex);
                alarmHabit.timestamp = c.getLong(TimeStampIDColIndex);
                alarmHabit.star = c.getInt(starColIndex);
                alarmHabits.add(alarmHabit);
                //i = i + 1;
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
        return alarmHabits;
    }

    //Метод возвращает заппись из таблицы привычек (dbhabits) по ID записи
    //Логика аналогична методу getData, только массив всегда состоит из одной записи
    public Habits getRecord(SQLiteDatabase db, int id){
        Habits habit = new Habits();
        String selection = "id = ?";
        String sid = String.valueOf(id);
        String[] selectionArgs = new String[] { sid };
         // делаем запрос всех данных из таблицы myhabits, получаем Cursor
        Cursor c = db.query("dbhabits", null, selection, selectionArgs, null, null, null);

        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");
            int descriptionColIndex = c.getColumnIndex("description");

            int obligatoryColIndex = c.getColumnIndex("obligatory");
            int optionalColIndex = c.getColumnIndex("optional");
            int knowledgeColIndex = c.getColumnIndex("knowledge");

            int photoIDColIndex = c.getColumnIndex("photoid");
            int alarmstatusColIndex = c.getColumnIndex("alarmstatus");
            int StatusColIndex = c.getColumnIndex("status");
            int alarmHoursColIndex = c.getColumnIndex("alarmhours");
            int alarmMinutesColIndex = c.getColumnIndex("alarmminutes");
            int TimeColIndex = c.getColumnIndex("time");
            int TimeStampColIndex = c.getColumnIndex("timestamp");
            int starColIndex = c.getColumnIndex("star");

            habit.ID = c.getInt(idColIndex);
            habit.name = c.getString(nameColIndex);
            habit.description = c.getString(descriptionColIndex);

            habit.obligatory = c.getString(obligatoryColIndex);
            habit.optional = c.getString(optionalColIndex);
            habit.knowledge = c.getString(knowledgeColIndex);

            habit.photoId = c.getInt(photoIDColIndex);
            habit.alarmstatus = c.getInt(alarmstatusColIndex);
            habit.status = c.getInt(StatusColIndex);
            habit.alarmhours = c.getInt(alarmHoursColIndex);
            habit.alarmminutes = c.getInt(alarmMinutesColIndex);
            habit.time = c.getLong(TimeColIndex);
            habit.timestamp = c.getLong(TimeStampColIndex);
            habit.star = c.getInt(starColIndex);
        }

        c.close();
        return habit;
    }

    //Метод обновляет поле status по id записи
    public void updateHabitStatus(SQLiteDatabase db, int id, int status){
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        // обновляем по id
        try {
            db.beginTransaction();
        int updCount = db.update("dbhabits", cv, "id = ?",  new String[] { String.valueOf(id) });
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
      }

    //Метод обновляет поле alarmStatus по id записи
    public void updateHabitAlarmStatus(SQLiteDatabase db, int id, int alarmstatus){
        ContentValues cv = new ContentValues();
        cv.put("alarmstatus", alarmstatus);
        // обновляем по id
        try {
            db.beginTransaction();
            int updCount = db.update("dbhabits", cv, "id = ?",  new String[] { String.valueOf(id) });
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    //Метод обновляет время (часы и минуты) на которое установлена нотификация для привычки
    public void updateAlarmTime(SQLiteDatabase db, int id, int hours, int minutes){
        ContentValues cv = new ContentValues();
        cv.put("alarmhours", hours);
        cv.put("alarmminutes", minutes);

        // обновляем по id
        try {
            db.beginTransaction();
            int updCount = db.update("dbhabits", cv, "id = ?",  new String[] { String.valueOf(id) });
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

   //Метод обновляет поле timestamp - момент с которого привычка активирована
    public void updateTimeStamp(SQLiteDatabase db, int id, long timestamp){
        ContentValues cv = new ContentValues();
        cv.put("timestamp", timestamp);
        // обновляем по id
        try {
            db.beginTransaction();
            int updCount = db.update("dbhabits", cv, "id = ?",  new String[] { String.valueOf(id) });
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    //Метод обновляет два поля time - суммарное время активности привычки за все предыдущие операции активации/деактивации
    // и поле timestamp - момент с которого привычка активирована
    public void updateTimeTimeStamp(SQLiteDatabase db, int id, long time, long timestamp){
        ContentValues cv = new ContentValues();
        cv.put("time", time);
        cv.put("timestamp", timestamp);
        // обновляем по id
        try {
            db.beginTransaction();
            int updCount = db.update("dbhabits", cv, "id = ?",  new String[] { String.valueOf(id) });
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    //Метод обновляет поле Star - используется для назначения звезды при выполнении привычки
    public void updateStar(SQLiteDatabase db, int id, int star){
        ContentValues cv = new ContentValues();
        cv.put("star", star);
        // обновляем по id
        try {
            db.beginTransaction();
            int updCount = db.update("dbhabits", cv, "id = ?",  new String[] { String.valueOf(id) });
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }


    //Метод возвращает данные из таблицы с настройками
    public int getSetting(SQLiteDatabase db, int id){

        String selection = "id = ?";
        String sid = String.valueOf(id);
        String[] selectionArgs = new String[] { sid };
        // делаем запрос всех данных из таблицы myhabits, получаем Cursor
        Cursor c = db.query("dbsettings", null, selection, selectionArgs, null, null, null);
        int settingValue = 3;

        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
             int valueColIndex = c.getColumnIndex("value");

            settingValue = c.getInt(valueColIndex);
        }

        c.close();
        return settingValue;
    }

    //Метод добавляющий запись в таблицу с настройками
    public void setSetting(SQLiteDatabase db, int id, int settingValue){
        ContentValues cv = new ContentValues();
        cv.put("value", settingValue);
        // обновляем по id
        try {
            db.beginTransaction();
            int updCount = db.update("dbsettings", cv, "id = ?",  new String[] { String.valueOf(id) });
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }


//Заполнение таблицы данными из файла values/stings.xml
    public void InitializeTable(SQLiteDatabase db){
        List<Habits> habits = new ArrayList<>();

      //Добавляем в массив новые данные
        habits.add(new Habits(context.getString(R.string.habitName_01), context.getString(R.string.habitDescription_01), context.getString(R.string.habitObligatory_01), context.getString(R.string.habitOptional_01), context.getString(R.string.habitKnowledge_01), R.drawable.greens, 0, 0, 0, 0, 0, 0, 0 ));
        habits.add(new Habits(context.getString(R.string.habitName_02), context.getString(R.string.habitDescription_02), context.getString(R.string.habitObligatory_02), context.getString(R.string.habitOptional_02), context.getString(R.string.habitKnowledge_02), R.drawable.sleep,0, 0, 0, 0, 0, 0, 0 ));
        habits.add(new Habits(context.getString(R.string.habitName_03), context.getString(R.string.habitDescription_03), context.getString(R.string.habitObligatory_03), context.getString(R.string.habitOptional_03), context.getString(R.string.habitKnowledge_03), R.drawable.water,  0, 0, 0, 0, 0, 0, 0 ));

        habits.add(new Habits(context.getString(R.string.habitName_05), context.getString(R.string.habitDescription_05), context.getString(R.string.habitObligatory_05), context.getString(R.string.habitOptional_05), context.getString(R.string.habitKnowledge_05), R.drawable.plate,  0, 0, 0, 0, 0, 0, 0 ));
        habits.add(new Habits(context.getString(R.string.habitName_06), context.getString(R.string.habitDescription_06), context.getString(R.string.habitObligatory_06), context.getString(R.string.habitOptional_06), context.getString(R.string.habitKnowledge_06), R.drawable.nuts, 0, 0, 0, 0, 0, 0, 0 ));
        habits.add(new Habits(context.getString(R.string.habitName_07), context.getString(R.string.habitDescription_07),context.getString(R.string.habitObligatory_07), context.getString(R.string.habitOptional_07), context.getString(R.string.habitKnowledge_07),R.drawable.sport, 0, 0, 0, 0, 0, 0, 0 ));

        habits.add(new Habits(context.getString(R.string.habitName_04), context.getString(R.string.habitDescription_04), context.getString(R.string.habitObligatory_04), context.getString(R.string.habitOptional_04), context.getString(R.string.habitKnowledge_04), R.drawable.stress,  0, 0, 0, 0, 0, 0, 0 ));


        habits.add(new Habits(context.getString(R.string.habitName_08), context.getString(R.string.habitDescription_08),context.getString(R.string.habitObligatory_08), context.getString(R.string.habitOptional_08), context.getString(R.string.habitKnowledge_08),R.drawable.fruits, 0, 0, 0, 0, 0, 0, 0 ));
        habits.add(new Habits(context.getString(R.string.habitName_09), context.getString(R.string.habitDescription_09),context.getString(R.string.habitObligatory_09), context.getString(R.string.habitOptional_09), context.getString(R.string.habitKnowledge_09),R.drawable.cereal,  0, 0, 0, 0, 0, 0, 0 ));
        habits.add(new Habits(context.getString(R.string.habitName_10), context.getString(R.string.habitDescription_10),context.getString(R.string.habitObligatory_10), context.getString(R.string.habitOptional_10), context.getString(R.string.habitKnowledge_09),R.drawable.shop,  0, 0, 0, 0, 0, 0, 0 ));
        habits.add(new Habits(context.getString(R.string.habitName_11), context.getString(R.string.habitDescription_11),context.getString(R.string.habitObligatory_11), context.getString(R.string.habitOptional_11), context.getString(R.string.habitKnowledge_10),R.drawable.salt,  0, 0, 0, 0, 0, 0, 0 ));
        habits.add(new Habits(context.getString(R.string.habitName_12), context.getString(R.string.habitDescription_12),context.getString(R.string.habitObligatory_12), context.getString(R.string.habitOptional_12), context.getString(R.string.habitKnowledge_11),R.drawable.badfood,  0, 0, 0, 0, 0, 0, 0 ));

        //Считываем массив
        for (Habits habit : habits) {

            // создаем объект для хранения данных
            ContentValues cv = new ContentValues();

            // получаем данные из строки массива
            String name = habit.getName();
            String description = habit.getDescription();
            String obligatory = habit.getObligatory();
            String optional = habit.getOptional();
            String knowledge = habit.getKnowledge();
            int photoID = habit.getPhotoID();
            int status = habit.getStatus();
            int alarmstatus = habit.getAlarmStatus();
            int alarmhours = habit.getAlarmHours();
            int alarmminutes = habit.getAlarmMinutes();
            long time = habit.getTime();
            long timestamp = habit.getTime();

            Log.d(LOG_TAG, "--- Insert in dbhabits: ---");

            // готовим данные для вставки в виде пар: наименование столбца - значение
            cv.put("name", name);
            cv.put("description", description);
            cv.put("obligatory", obligatory);
            cv.put("optional", optional);
            cv.put("knowledge", knowledge);
            cv.put("photoid", photoID);
            cv.put("status", status);
            cv.put("alarmstatus", alarmstatus);
            cv.put("alarmhours", alarmhours);
            cv.put("alarmminutes", alarmminutes);
            cv.put("time", time);
            cv.put("timestamp", timestamp);



            // вставляем запись в таблицу БД (и получаем ее ID)
            long rowID = db.insert("dbhabits", null, cv);
        }
    }


    //Заполнение таблицы для хранения настроек значениями по умолчанию
    public void InitializeSettings(SQLiteDatabase db){

        // Создаем контейнер
        ContentValues cv = new ContentValues();

        int notifications = 1; //on
        int sound = 1; //on
        int logo = 1; //ong
        int hours = 0; //off

        cv.put("id", 1); //Добавлеем значения в контейнер
        cv.put("value", notifications);
        long rowID = db.insert("dbsettings", null, cv);  //вставляем контейнер в таблицу с настройками

        cv.put("id", 2); //sound
        cv.put("value", sound); //sound ON
         rowID = db.insert("dbsettings", null, cv);

        cv.put("id", 3); //logo
        cv.put("value", logo); //logo ON
        rowID = db.insert("dbsettings", null, cv);

        cv.put("id", 4); //hours
        cv.put("value", hours); //hours OFF
        rowID = db.insert("dbsettings", null, cv);


    }

}
