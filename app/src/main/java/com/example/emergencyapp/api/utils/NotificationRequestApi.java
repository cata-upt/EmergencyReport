package com.example.emergencyapp.api.utils;

public class NotificationRequestApi {
    private String token;
    private String title;
    private String body;
    private ExtraDataNotifications extraDataNotifications;

    public NotificationRequestApi(String token, String title, String body) {
        this.token = token;
        this.title=title;
        this.body = body;
        this.extraDataNotifications = new ExtraDataNotifications();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ExtraDataNotifications getExtraDataNotifications() {
        return extraDataNotifications;
    }

    public void setExtraDataNotifications(ExtraDataNotifications extraDataNotifications) {
        this.extraDataNotifications = extraDataNotifications;
    }
}
