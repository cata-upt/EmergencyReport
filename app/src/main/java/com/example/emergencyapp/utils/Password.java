package com.example.emergencyapp.utils;

public class Password {
    String password;
    byte[] salt;

    public Password() {
    }

    public Password(String password, byte[] salt) {
        this.password = password;
        this.salt = salt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
}
