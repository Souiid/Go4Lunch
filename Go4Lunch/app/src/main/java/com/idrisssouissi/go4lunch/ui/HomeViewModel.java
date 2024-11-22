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
import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.RestaurantRepository;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.data.UserItem;
import com.idrisssouissi.go4lunch.data.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;

import kotlin.Triple;

public class HomeViewModel extends ViewModel {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final FirebaseApiService firebaseApiService;
    private final MutableLiveData<Boolean> isUserConnected = new MutableLiveData<>();


    MediatorLiveData<Pair<List<Restaurant>, List<User>>> uiStateLiveData = new MediatorLiveData<>();
    LiveData<List<Restaurant>> restaurantsLiveData;
    LiveData<List<User>> usersLiveData;
    @Inject
    public HomeViewModel(RestaurantRepository restaurantRepository, UserRepository userRepository, FirebaseApiService firebaseApiService) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;

        // Initialiser les LiveData
        this.restaurantsLiveData = restaurantRepository.getRestaurantsLiveData();
        this.usersLiveData = userRepository.getUsersLiveData();
        this.firebaseApiService = firebaseApiService;

        // Charger les utilisateurs au démarrage
        userRepository.getAllUsers();
        checkUserConnection();
        // Ajouter des sources au MediatorLiveData
        uiStateLiveData.addSource(restaurantsLiveData, restaurants -> {
            onNewData(restaurants, usersLiveData.getValue());
        });

        uiStateLiveData.addSource(usersLiveData, users -> {
            onNewData(restaurantsLiveData.getValue(), users);
        });
    }

    public LiveData<List<Restaurant>> getRestaurantsByFetch(Double latitude, Double longitude) throws IOException {
        Log.d("aaa", "Fetching restaurants for lat: " + latitude + ", lon: " + longitude);
        restaurantRepository.updatePosition(latitude, longitude);
        return restaurantRepository.getRestaurantsLiveData();
    }

    public void onNewData(List<Restaurant> restaurants, List<User> users) {
        if (restaurants != null && users != null) {
            uiStateLiveData.setValue(new Pair<>(restaurants, users));
        } else {
            Log.d("HomeViewModel", "onNewData called but one or both lists are null");
        }
    }

    public LiveData<Boolean> getUserConnectionStatus() {
        return isUserConnected;
    }

    public void checkUserConnection() {
        isUserConnected.setValue(firebaseApiService.isUserConnected());
    }


    public void signOut() {
        firebaseApiService.signOut();
        isUserConnected.setValue(false);
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

    // Tri par note (rating)
    public void sortRestaurantsByNote(Boolean isAscendant) {
        List<Restaurant> currentRestaurants = restaurantsLiveData.getValue();
        if (currentRestaurants != null) {
            Collections.sort(currentRestaurants, new Comparator<Restaurant>() {
                @Override
                public int compare(Restaurant r1, Restaurant r2) {
                    if (isAscendant) {
                        return Float.compare(r2.getNote().floatValue(), r1.getNote().floatValue());

                    }else {
                        return Float.compare(r1.getNote().floatValue(), r2.getNote().floatValue());

                    }
                }
            });
            // Met à jour les restaurants triés dans LiveData
            ((MutableLiveData<List<Restaurant>>) restaurantsLiveData).setValue(currentRestaurants);
        }
    }

    public void sortRestaurantsByDistance() {
        List<Restaurant> currentRestaurants = restaurantsLiveData.getValue();
        if (currentRestaurants != null) {
            Collections.sort(currentRestaurants, new Comparator<Restaurant>() {
                @Override
                public int compare(Restaurant r1, Restaurant r2) {
                    if (r1.getDistance().isPresent() && r2.getDistance().isPresent()) {
                        return Float.compare(r1.getDistance().get(), r2.getDistance().get()); // Tri croissant par distance
                    } else if (r1.getDistance().isPresent()) {
                        return -1;
                    } else if (r2.getDistance().isPresent()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
            // Met à jour les restaurants triés dans LiveData
            ((MutableLiveData<List<Restaurant>>) restaurantsLiveData).setValue(currentRestaurants);
        }
    }

    public Triple<String, String, String> getDistantRestaurantName(String restaurantId) throws IOException {
        return restaurantRepository.getRestaurantContact(restaurantId);
    }

    // Tri par nom
    public void sortRestaurantsByName(Boolean isAscendant) {
        List<Restaurant> currentRestaurants = restaurantsLiveData.getValue();
        if (currentRestaurants != null) {
            Collections.sort(currentRestaurants, new Comparator<Restaurant>() {
                @Override
                public int compare(Restaurant r1, Restaurant r2) {
                    if (isAscendant) {
                        return r1.getName().compareToIgnoreCase(r2.getName());
                    }else {
                        return r2.getName().compareToIgnoreCase(r1.getName());
                    }
                }
            });
            // Met à jour les restaurants triés dans LiveData
            ((MutableLiveData<List<Restaurant>>) restaurantsLiveData).setValue(currentRestaurants);
        }
    }


    public void filterRestaurantsByQuery(String query) {
        List<Restaurant> currentRestaurants = restaurantsLiveData.getValue();
        if (currentRestaurants != null) {
            List<Restaurant> filteredRestaurants = new ArrayList<>();
            for (Restaurant restaurant : currentRestaurants) {
                if (restaurant.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredRestaurants.add(restaurant);
                }
            }
            // Met à jour la liste filtrée dans LiveData
            ((MutableLiveData<List<Restaurant>>) restaurantsLiveData).setValue(filteredRestaurants);
        }
    }


    public Float getDistance(LatLng lastLocation, LatLng restaurantLocation) {
        float[] results = new float[1];
        Location.distanceBetween(lastLocation.latitude, lastLocation.longitude, restaurantLocation.latitude, restaurantLocation.longitude, results);
        return results[0];
    }

    public String formatDistance(Float distance) {
        if (distance < 1000) {
            return Math.round(distance) + " m";
        } else {
            Float distanceInKilometers = distance / 1000;
            return String.format("%.1f km", distanceInKilometers);
        }
    }


    public static class Factory implements ViewModelProvider.Factory {
        private final RestaurantRepository restaurantRepository;
        private final UserRepository userRepository;
        private final FirebaseApiService firebaseApiService;

        @Inject
        public Factory(RestaurantRepository restaurantRepository, UserRepository userRepository, FirebaseApiService firebaseApiService) {
            this.restaurantRepository = restaurantRepository;
            this.userRepository = userRepository;
            this.firebaseApiService = firebaseApiService;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(HomeViewModel.class)) {
                return (T) new HomeViewModel(restaurantRepository, userRepository, firebaseApiService);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

