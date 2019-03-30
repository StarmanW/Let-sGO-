package com.tarcrsd.letsgo.Models;

import com.google.firebase.firestore.DocumentReference;

import java.time.LocalDate;
import java.util.Date;

public class EventAttendees {
    private String id;
    private DocumentReference userUID;
    private DocumentReference eventID;
    private Date eventDate;
    private int status;

    public EventAttendees() {
    }

    public EventAttendees(String id, DocumentReference userUID, DocumentReference eventID, Date eventDate, int status) {
        this.id = id;
        this.userUID = userUID;
        this.eventID = eventID;
        this.eventDate = eventDate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentReference getUserUID() {
        return userUID;
    }

    public void setUserUID(DocumentReference userUID) {
        this.userUID = userUID;
    }

    public DocumentReference getEventID() {
        return eventID;
    }

    public void setEventID(DocumentReference eventID) {
        this.eventID = eventID;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getStatus() {
        String statusStr = "None";

        switch (status) {
            case 0:
                statusStr = "Absent";
                break;
            case 1:
                statusStr = "Pending";
                break;
            case 2:
                statusStr = "Present";
                break;
        }
        return statusStr;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
