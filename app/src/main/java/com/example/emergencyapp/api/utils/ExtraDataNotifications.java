package com.example.emergencyapp.api.utils;

import java.util.HashMap;
import java.util.Map;

public class ExtraDataNotifications {
    Map<String, String> data = new HashMap<>();

    public ExtraDataNotifications(Map<String, String> data) {
        this.data = data;
    }

    public ExtraDataNotifications() {
    }

    public void addData(String key, String value){
        data.put(key, value);
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
