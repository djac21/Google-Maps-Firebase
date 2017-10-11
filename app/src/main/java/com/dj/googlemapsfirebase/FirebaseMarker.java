package com.dj.googlemapsfirebase;

import java.util.HashMap;

public class FirebaseMarker {

    private String snippet, title;
    private double latitude, longitude, price;

    public FirebaseMarker() {

    }

    public FirebaseMarker(String snippet, String title, double latitude, double longitude, double price) {
        this.snippet = snippet;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.price = price;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public HashMap<String, Object> toFirebaseObject()  {
        HashMap<String,Object> add_items_firebase =  new HashMap<>();
        add_items_firebase.put("title", title);
        add_items_firebase.put("snippet", snippet);
        add_items_firebase.put("price", price);
        add_items_firebase.put("longitude", longitude);
        add_items_firebase.put("latitude", latitude);

        return add_items_firebase;
    }
}
