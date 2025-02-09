package com.idrisssouissi.go4lunch.data;

import java.time.LocalTime;
import java.util.Optional;

public class Restaurant {

    String id;
    String name;
    String address;
    double latitude;
    double longitude;
    Optional<String> type;
    String photoUrl;
    Optional<Float> distance;
    LocalTime[] openHours;
    Optional<String> phoneNumber;
    Optional<String> website;
    Optional<Integer> note;
    Optional<Integer> numberOfUsers;

    public Restaurant(String id, String name, String address, double latitude, double longitude, String type,
                      String photoUrl,
                      LocalTime[] openHours,
                      Optional<String> phoneNumber,
                      Optional<String> website,
                      Optional<Integer> note,
                      Optional<Integer> numberOfUsers) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = Optional.ofNullable(type);
        this.photoUrl = photoUrl;
        this.distance = Optional.empty();
        this.openHours = openHours;
        this.phoneNumber = phoneNumber;
        this.website = website;
        this.note = note;
        this.numberOfUsers = numberOfUsers;

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

    public void setDistance(Float distance) {
        this.distance = Optional.ofNullable(distance);
    }

    public Optional<Float> getDistance() {
        return distance;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public LocalTime[] getOpenHours() {
        return openHours;
    }

    public void setNote(Integer note) {
        this.note = Optional.ofNullable(note);
    }

    public Number getNote() {
        return note.orElse(0);
    }

    public void setNumberOfUsers(Integer numberOfUsers) {
        this.numberOfUsers = Optional.ofNullable(numberOfUsers);
    }

    public Number getNumberOfUsers() {
        return numberOfUsers.orElse(0);
    }

    public void setOpenHours(LocalTime[] openHours) {
        this.openHours = openHours;
    }

}
