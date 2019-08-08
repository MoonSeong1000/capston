package com.example.test3;

import java.io.Serializable;

public class Search_Dictionary implements Serializable {

    private String start_name;
    private String end_name;
    private double start_lat,star_lon,end_lat,end_lon;

    public String getStart_name() {
        return start_name;
    }

    public void setStart_name(String start_name) {
        this.start_name = start_name;
    }

    public String getEnd_name() {
        return end_name;
    }

    public void setEnd_name(String end_name) {
        this.end_name = end_name;
    }

    public double getStart_lat() {
        return start_lat;
    }

    public void setStart_lat(double start_lat) {
        this.start_lat = start_lat;
    }

    public double getStar_lon() {
        return star_lon;
    }

    public void setStar_lon(double star_lon) {
        this.star_lon = star_lon;
    }

    public double getEnd_lat() {
        return end_lat;
    }

    public void setEnd_lat(double end_lat) {
        this.end_lat = end_lat;
    }

    public double getEnd_lon() {
        return end_lon;
    }

    public void setEnd_lon(double end_lon) {
        this.end_lon = end_lon;
    }

    public Search_Dictionary(String start_name, String end_name, double start_lat, double star_lon, double end_lat, double end_lon) {
        this.start_name = start_name;
        this.end_name = end_name;
        this.start_lat = start_lat;
        this.star_lon = star_lon;
        this.end_lat = end_lat;
        this.end_lon = end_lon;
    }
}
