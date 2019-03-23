package com.tarcrsd.letsgo.Models;

import com.google.firebase.firestore.DocumentReference;

public class EventOrganizer {

    private String id;
    private DocumentReference userID;
    private DocumentReference eventID;

    public EventOrganizer() {
    }

    public EventOrganizer(String id, DocumentReference userID, DocumentReference eventID) {
        this.id = id;
        this.userID = userID;
        this.eventID = eventID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentReference getUserID() {
        return userID;
    }

    public void setUserID(DocumentReference userID) {
        this.userID = userID;
    }

    public DocumentReference getEventID() {
        return eventID;
    }

    public void setEventID(DocumentReference eventID) {
        this.eventID = eventID;
    }
}
