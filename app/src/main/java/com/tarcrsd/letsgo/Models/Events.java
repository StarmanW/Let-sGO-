package com.tarcrsd.letsgo.Models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class Events {
    private String eventID;
    private String name;
    private String description;
    private String image;
    private Date date;
    private Date time;
    private String location;
    private String locality;

    public Events() {
    }

    public Events(String eventID, String name, String description, String image, Date date, Date time, String location, String locality) {
        this.eventID = eventID;
        this.name = name;
        this.description = description;
        this.image = image;
        this.date = date;
        this.time = time;
        this.location = location;
        this.locality = locality;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }
}
