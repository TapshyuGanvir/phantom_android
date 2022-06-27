package com.sixsimplex.trail;

import android.location.Location;

import java.io.Serializable;

public class TrailLatLng implements Serializable {
    double latitude,longitude,accuracy,speed;

    public TrailLatLng() {
    }

    public TrailLatLng(Location location) {
        this.latitude= location.getLatitude();
        this.longitude= location.getLongitude();
        this.accuracy= location.getAccuracy();
        this.speed= location.getSpeed();
    }

    public TrailLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy=1;
        this.speed=0;
    }

    public TrailLatLng(double latitude, double longitude, double accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.speed=0;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
