package com.idrisssouissi.go4lunch.data;

import android.graphics.Bitmap;

import java.util.Optional;

public class Restaurant {

    String id;
    String name;
    String address;
    double latitude;
    double longitude;
    Optional<String> type;
    String photoUrl;
    Optional<String> distance;
    String openHours;
    Optional<String> phoneNumber;
    Optional<String> website;

    public Restaurant(String id, String name, String address, double latitude, double longitude, String type,
                      String photoUrl, String openHours, Optional<String> phoneNumber, Optional<String> website) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = Optional.ofNullable(type); // Utilisation d'Optional pour le type
        this.photoUrl = photoUrl;
        this.distance = Optional.empty();
        this.openHours = openHours;
        this.phoneNumber = phoneNumber;
        this.website = website;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Optional<String> getType() {
        return type;
    }

    public void setDistance(String distance) {
        this.distance = Optional.ofNullable(distance);
    }

    public Optional<String> getDistance() {
        return distance;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getOpenHours() {
        return openHours;
    }

    public void setOpenHours(String openHours) {
        this.openHours = openHours;
    }

    public Optional<String> getPhoneNumber() {
        return phoneNumber;
    }

    public Optional<String> getWebsite() {
        return website;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = Optional.ofNullable(phoneNumber);
    }

    public void setWebsite(String website) {
        this.website = Optional.ofNullable(website);
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
