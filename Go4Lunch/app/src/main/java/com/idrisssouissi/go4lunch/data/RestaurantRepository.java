package com.idrisssouissi.go4lunch.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import kotlin.Triple;

public class RestaurantRepository {

    private List<Restaurant> restaurants = new ArrayList<>();
    private final RestaurantApiService restaurantApiService;

    @Inject
    public RestaurantRepository(RestaurantApiService restaurantApiService) {
        this.restaurantApiService = restaurantApiService;
    }

    public List<Restaurant> getRestaurantsByLocation(Double latitude, Double longitude) throws IOException {

        restaurants = restaurantApiService.fetchNearbyRestaurants(latitude, longitude);
        return restaurants;
    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }


    public Restaurant getRestaurantById(String restaurantId) {
        if (restaurants != null) {
            for (Restaurant restaurant : restaurants) {
                if (restaurant.getId().equals(restaurantId)) {
                    return restaurant;
                }
            }
        }
        return null;
    }

    public Triple<String, String, String> getRestaurantContact(String restaurantId) throws IOException {
        return restaurantApiService.getRestaurantDetailsFromId(restaurantId, false);
    }

}
