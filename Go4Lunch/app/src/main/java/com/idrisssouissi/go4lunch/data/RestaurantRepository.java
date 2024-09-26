package com.idrisssouissi.go4lunch.data;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;

public class RestaurantRepository {

    private MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
    private RestaurantApiService restaurantApiService;
    private MutableLiveData<LatLng> lastLocation = new MutableLiveData<>();

    @Inject
    public RestaurantRepository(RestaurantApiService restaurantApiService) {
        this.restaurantApiService = restaurantApiService;
    }

    public void updatePosition(Double latitude, Double longitude) {
        restaurantApiService.fetchNearbyRestaurants(latitude, longitude, restaurants -> {
            Log.d("aaa", "Fetched Restaurants in repo" + restaurants.size() + " restaurants");
            restaurantsLiveData.postValue(restaurants);
        });
    }

    public void setLastLocation(LatLng location) {
        lastLocation.postValue(location);
    }

    public MutableLiveData<List<Restaurant>> getRestaurantsLiveData() {
        return restaurantsLiveData;
    }

    public Restaurant getRestaurantById(String restaurantId) {
        List<Restaurant> restaurants = restaurantsLiveData.getValue();
        if (restaurants != null) {
            for (Restaurant restaurant : restaurants) {
                if (restaurant.getId().equals(restaurantId)) {
                    return restaurant;
                }
            }
        }
        return null;
    }

    public void getRestaurantContact(String restaurantId, Consumer<Optional<String>> website, Consumer<Optional<String>> phoneNumber) {
        restaurantApiService.getRestaurantDetailsFromId(restaurantId, website, phoneNumber);
    }

    public MutableLiveData<LatLng> getLastLocation() {
        return lastLocation;
    }
}
