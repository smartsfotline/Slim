package pt.personaltrainer;
//Класс определяющий формат данных привычки. Набор атрибутов и методы сетеры и гетеры

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aliaksandr.shakavets on 17-Apr-17.
 */

public class Habits {
    int ID;
    String name;
    String description;
    String obligatory;
    String optional;
    String knowledge;
    int photoId;
    int alarmstatus;
    int status;
    int alarmhours;
    int alarmminutes;
    long time;
    long timestamp;
    int star;

    public Habits(String name, String description, String obligatory, String optional, String knowledge,int photoId, int status, int alarmstatus,  int alarmhours, int alarmminutes, long time, long timestamp, int star) {
        this.name = name;
        this.description = description;
        this.obligatory = obligatory;
        this.optional = optional;
        this.knowledge = knowledge;
        this.photoId = photoId;
        this.alarmstatus = alarmstatus;
        this.status = status;
        this.alarmhours = alarmhours;
        this.alarmminutes = alarmminutes;
        this.time = time;
        this.timestamp = timestamp;
        this.star = star;
    }

    public int getID(){
        return this.ID;
    }

    public String getName(){
        return this.name;
    }
    public String getDescription(){
        return this.description;
    }

    public String getObligatory(){
        return this.obligatory;
    }
    public String getOptional(){
        return this.optional;
    }
    public String getKnowledge(){
        return this.knowledge;
    }

    public int getPhotoID(){
        return this.photoId;
    }

    public int getAlarmStatus(){
        return this.alarmstatus;
    }

    public int getStatus(){
        return this.status;
    }

    public int getAlarmHours()   { return this.alarmhours; }
    public int getAlarmMinutes(){
        return this.alarmminutes;
    }

    public long getTime(){
        return this.time;
    }

    public long getTimeStamp(){
        return this.timestamp;
    }

    public long getStar(){
        return this.star;
    }

    public Habits()
    {}

        }