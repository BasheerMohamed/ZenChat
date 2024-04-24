package com.basheer.whatsappclone.model;

public class ChatList {
    private String userID;
    private String userName;
    private String description;
    private String date;
    private String urlProfile;
    private String userPhone;
    public ChatList() {
    }

    public ChatList(String userID, String userName, String description, String date, String urlProfile, String userPhone) {
        this.userID = userID;
        this.userName = userName;
        this.description = description;
        this.date = date;
        this.urlProfile = urlProfile;
        this.userPhone = userPhone;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrlProfile() {
        return urlProfile;
    }

    public void setUrlProfile(String urlProfile) {
        this.urlProfile = urlProfile;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}
