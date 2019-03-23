package com.tarcrsd.letsgo.Models;

public class User {
    private String userUID;
    private String name;
    private String contact;
    private String address;
    private String profileImg;

    public User() {
    }

    public User(String userUID, String name, String contact, String address, String profileImg) {
        this.userUID = userUID;
        this.name = name;
        this.contact = contact;
        this.address = address;
        this.profileImg = profileImg;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}
