package com.example.emergencyapp.entities;

public class UserLocation {

    private Double latitude, longitude, altitude;
    private Long time;
    private Float accuracy, speed;

    public UserLocation() {
    }

    public UserLocation(Double latitude, Double longitude, Long time, Float accuracy, Double altitude, Float speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.speed = speed;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }
}
