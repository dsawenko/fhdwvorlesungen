package de.sawenko.fhdw.vorlesungen.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import biweekly.property.DateEnd;
import biweekly.property.DateStart;

/**
 * Created by Daniel on 22.04.2016.
 */
public class Vorlesung {

    private DateStart dateStart;
    private DateEnd dateEnd;
    private Module module;
    private Lecturer lecturer;
    private String room;
    private String summary;

    public Vorlesung(DateStart dateStart, DateEnd dateEnd, Module module, Lecturer lecturer, String room, String summary) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.module = module;
        this.lecturer = lecturer;
        this.room = room;
        this.summary = summary;
    }

    public boolean isBefore(Vorlesung v) {
        return dateStart.getValue().getTime() < v.dateStart.getValue().getTime();
    }

  
    public String getDate() {
        DateFormat dfdate = new SimpleDateFormat("dd.MM.yyyy");
        String dateStartStr = (dateStart == null) ? null : dfdate.format(dateStart.getValue());
        return dateStartStr;
    }

    public String getDateTime() {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        DateFormat dftime = new SimpleDateFormat("HH:mm");
        dftime.setTimeZone(TimeZone.getTimeZone( "Europe/Berlin"));
        String dateStartStr = (dateStart == null) ? null : df.format(dateStart.getValue());
        String dateEndStr = (dateEnd == null) ? null : dftime.format(dateEnd.getValue());
        return dateStartStr + " bis " + dateEndStr;
    }

    public String getTime() {
        DateFormat dftime = new SimpleDateFormat("HH:mm");
        dftime.setTimeZone(TimeZone.getTimeZone( "Europe/Berlin"));
        String dateStartStr = (dateStart == null) ? null : dftime.format(dateStart.getValue());
        String dateEndStr = (dateEnd == null) ? null : dftime.format(dateEnd.getValue());
        return dateStartStr + " bis " + dateEndStr;
    }

    public DateStart getDateStart() {
        return dateStart;
    }

    public DateEnd getDateEnd() {
        return dateEnd;
    }

    public Module getModule() {
        return module;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public String getRoom() {
        return room;
    }
    
    public String toString() {
    	if (module != null) {
    		String s = module.toString();        
    		return s != "" ? s : summary;
    	}
    	return "";
    }


}
