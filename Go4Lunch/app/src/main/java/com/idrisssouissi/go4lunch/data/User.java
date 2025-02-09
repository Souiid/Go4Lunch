package com.idrisssouissi.go4lunch.data;

import android.graphics.Bitmap;
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

    public Map<String, Object> getSelectedRestaurant() {
        return selectedRestaurant;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getSelectedRestaurantID() {
        return selectedRestaurant != null ? (String) selectedRestaurant.get("id") : null;
    }

    public List<String> getRestaurantLikeIDs() {
        return restaurantLikeIDs;
    }

}
