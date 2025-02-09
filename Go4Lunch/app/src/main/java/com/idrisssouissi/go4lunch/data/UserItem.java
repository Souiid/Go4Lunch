package com.idrisssouissi.go4lunch.data;

public class UserItem {

    String id;
    String name;
    String restaurantName;
    String photoUrl;

    public UserItem(String id, String name, String restaurantName, String photoUrl) {
        this.id = id;
        this.name = name;
        this.restaurantName = restaurantName;
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}