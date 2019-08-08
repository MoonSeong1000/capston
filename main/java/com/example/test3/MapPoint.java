package com.example.test3;

public class MapPoint {
    private String Name;
    private double latitude;
    private double longtitude;

    public MapPoint(String Name, double latitude, double longtitude) {
        this.Name = Name;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }
}
