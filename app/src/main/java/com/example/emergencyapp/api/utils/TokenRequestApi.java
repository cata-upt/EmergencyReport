package com.example.emergencyapp.api.utils;

public class TokenRequestApi {
    private String token;
    private String userId;

    public TokenRequestApi(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
