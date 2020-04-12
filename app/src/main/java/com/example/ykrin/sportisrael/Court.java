package com.example.ykrin.sportisrael;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;


public class Court {
    private String title;
    private String description;
    private GeoPoint location;
    private String state;

    public Court(String title, String description, GeoPoint location, String state) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.state = state;
    }

    public Court(){
    }

    @Override
    public String toString() {
        return "Court{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", location=" + location +
                ", state='" + state + '\'' +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public LatLng getLatLng()
    {
        return new LatLng(this.location.getLatitude(), this.location.getLongitude());
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
