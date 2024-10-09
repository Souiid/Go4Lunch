package com.idrisssouissi.go4lunch.ui;

import android.location.Location;
import android.util.Log;
import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.maps.model.LatLng;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.RestaurantRepository;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.data.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class HomeViewModel extends ViewModel {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    MediatorLiveData<Pair<List<Restaurant>, List<User>>> liveDataMerger = new MediatorLiveData<>();
    LiveData<List<Restaurant>> restaurantsLiveData;
    LiveData<List<User>> usersLiveData;

    @Inject
    public HomeViewModel(RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;

        // Initialiser les LiveData
        this.restaurantsLiveData = restaurantRepository.getRestaurantsLiveData();
        this.usersLiveData = userRepository.getUsersLiveData();

        // Charger les utilisateurs au démarrage
        userRepository.getAllUsers();

        // Ajouter des sources au MediatorLiveData
        liveDataMerger.addSource(restaurantsLiveData, restaurants -> {
            onNewData(restaurants, usersLiveData.getValue());
        });

        liveDataMerger.addSource(usersLiveData, users -> {
            onNewData(restaurantsLiveData.getValue(), users);
        });
    }

    public LiveData<List<Restaurant>> getRestaurantsByFetch(Double latitude, Double longitude) {
        Log.d("aaa", "Fetching restaurants for lat: " + latitude + ", lon: " + longitude);
        restaurantRepository.updatePosition(latitude, longitude);
        return restaurantRepository.getRestaurantsLiveData();
    }

    public void onNewData(List<Restaurant> restaurants, List<User> users) {
        if (restaurants != null && users != null) {
            liveDataMerger.setValue(new Pair<>(restaurants, users));
        } else {
            Log.d("HomeViewModel", "onNewData called but one or both lists are null");
        }
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurantRepository.getRestaurantsLiveData();
    }

    public List<String> getAllSelectedRestaurantID(List<User> users, List<Restaurant> restaurants) {
        List<String> selectedRestaurantIDs = new ArrayList<>();

        for (User user : users) {
            String selectedRestaurantId = (String) user.getSelectedRestaurant().get("id");
            if (selectedRestaurantId != null) {
                for (Restaurant restaurant : restaurants) {
                    if (selectedRestaurantId.equals(restaurant.getId())) {
                        selectedRestaurantIDs.add(restaurant.getId());
                        break;
                    }
                }
            }
        }
        return selectedRestaurantIDs;
    }

    public MutableLiveData<LatLng> getLastLocation() {
        return restaurantRepository.getLastLocation();
    }

    public void refreshUsers() {
        userRepository.getAllUsers();
    }

    public void setLastLocation(Double latitude, Double longitude) {
        restaurantRepository.setLastLocation(new LatLng(latitude, longitude));
    }

    public String getDistance(LatLng lastLocation, LatLng restaurantLocation) {
        float[] results = new float[1];
        Location.distanceBetween(lastLocation.latitude, lastLocation.longitude, restaurantLocation.latitude, restaurantLocation.longitude, results);

        float distanceInMeters = results[0];

        if (distanceInMeters < 1000) {
            return Math.round(distanceInMeters) + " m";
        } else {
            float distanceInKilometers = distanceInMeters / 1000;
            return String.format("%.1f km", distanceInKilometers);
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final RestaurantRepository restaurantRepository;
        private final UserRepository userRepository;

        @Inject
        public Factory(RestaurantRepository restaurantRepository, UserRepository userRepository) {
            this.restaurantRepository = restaurantRepository;
            this.userRepository = userRepository;

        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(HomeViewModel.class)) {
                return (T) new HomeViewModel(restaurantRepository, userRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

