package com.idrisssouissi.go4lunch.data;

import android.graphics.Bitmap;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class User {

    String id;
    String email;
    String name;
    Map<String, Object> selectedRestaurant;
    String photoUrl;
    Optional<Bitmap> photo;
    List<String> restaurantLikeIDs;

    public User(String id, String email,String name, String photoUrl, Map<String, Object>  selectedRestaurant, List<String> restaurantLikeIDs) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.selectedRestaurant = selectedRestaurant;
        this.photoUrl = photoUrl;
        this.photo = Optional.empty();
        this.restaurantLikeIDs = restaurantLikeIDs;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Object> getSelectedRestaurant() {
        return selectedRestaurant;
    }

    public void setSelectedRestaurant(Map<String, Object> selectedRestaurant) {
        this.selectedRestaurant = selectedRestaurant;
    }


    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Optional<Bitmap> getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = Optional.ofNullable(photo);
    }

    public String getSelectedRestaurantID() {
        return selectedRestaurant != null ? (String) selectedRestaurant.get("id") : null;
    }

    Date getSelectedRestaurantDate() {
        Timestamp selectedRestaurantTimestamp = selectedRestaurant != null ? (Timestamp) selectedRestaurant.get("date") : null;
        return selectedRestaurantTimestamp != null ? selectedRestaurantTimestamp.toDate() : null;
    }

    public List<String> getRestaurantLikeIDs() {
        return restaurantLikeIDs;
    }

    public void setRestaurantLikeIDs(List<String> restaurantLikeIDs) {
        this.restaurantLikeIDs = restaurantLikeIDs;
    }
}
