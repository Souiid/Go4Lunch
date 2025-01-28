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
import com.google.firebase.Timestamp;
import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.LocationRepository;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.RestaurantRepository;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.data.UserRepository;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import javax.inject.Inject;

import kotlin.Triple;

public class HomeViewModel extends ViewModel {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final FirebaseApiService firebaseApiService;
    private final MutableLiveData<Boolean> isUserConnected = new MutableLiveData<>();
    private final LocationRepository locationRepository;


    MediatorLiveData<Pair<List<Restaurant>, List<User>>> uiStateLiveData = new MediatorLiveData<>();
    private final MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
    LiveData<List<User>> usersLiveData;

    @Inject
    public HomeViewModel(RestaurantRepository restaurantRepository, UserRepository userRepository, LocationRepository locationRepository, FirebaseApiService firebaseApiService) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;

        // Initialiser les LiveData
        this.restaurantsLiveData.setValue(restaurantRepository.getRestaurants());
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
        List<Restaurant> restaurants = restaurantRepository.getRestaurantsByLocation(latitude, longitude);
        restaurantsLiveData.postValue(restaurants);
        return restaurantsLiveData;
    }

    public void onNewData(List<Restaurant> restaurants, List<User> users) {
        if (restaurants != null && users != null) {
            uiStateLiveData.setValue(new Pair<>(restaurants, users));
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
        restaurantsLiveData.setValue(restaurantRepository.getRestaurants());
        return restaurantsLiveData;
    }

    public List<String> getAllSelectedRestaurantID(List<User> users, List<Restaurant> restaurants) {
        List<String> selectedRestaurantIDs = new ArrayList<>();

        for (User user : users) {

            String selectedRestaurantId = (String) user.getSelectedRestaurant().get("id");
            Timestamp restaurantDate = (Timestamp) user.getSelectedRestaurant().get("date");
            Timestamp nowInstant = Timestamp.now();

            if (selectedRestaurantId != null && restaurantDate != null) {
                // Récupérer la date actuelle sans l'heure
                Calendar nowCalendar = Calendar.getInstance();
                nowCalendar.setTime(nowInstant.toDate());
                nowCalendar.set(Calendar.HOUR_OF_DAY, 0);
                nowCalendar.set(Calendar.MINUTE, 0);
                nowCalendar.set(Calendar.SECOND, 0);
                nowCalendar.set(Calendar.MILLISECOND, 0);
                Date todayDate = nowCalendar.getTime(); // Aujourd’hui à 00:00:00

                // Récupérer la date du restaurant sans l'heure
                Calendar restaurantCalendar = Calendar.getInstance();
                restaurantCalendar.setTime(restaurantDate.toDate());
                restaurantCalendar.set(Calendar.HOUR_OF_DAY, 0);
                restaurantCalendar.set(Calendar.MINUTE, 0);
                restaurantCalendar.set(Calendar.SECOND, 0);
                restaurantCalendar.set(Calendar.MILLISECOND, 0);
                Date restaurantDay = restaurantCalendar.getTime(); // Jour du restaurant à 00:00:00

                // Vérifier si la date du restaurant est aujourd’hui
                if (restaurantDay.equals(todayDate)) {
                    // Ajouter la limite de 15h
                    restaurantCalendar.set(Calendar.HOUR_OF_DAY, 15);
                    restaurantCalendar.set(Calendar.MINUTE, 0);
                    restaurantCalendar.set(Calendar.SECOND, 0);
                    Timestamp fifteenOClockRestaurantDay = new Timestamp(restaurantCalendar.getTime());

                    for (Restaurant restaurant : restaurants) {
                        if (selectedRestaurantId.equals(restaurant.getId())
                                && restaurantDate.compareTo(fifteenOClockRestaurantDay) <= 0) { // Vérifie qu'on est avant 15h

                            selectedRestaurantIDs.add(restaurant.getId());
                            break;
                        }
                    }
                }
            }
        }

        return selectedRestaurantIDs;
    }

    public MutableLiveData<LatLng> getLastLocation() {
        return locationRepository.getLastLocation();
    }

    public void refreshUsers() {
        userRepository.getAllUsers();
    }

    public void setLastLocation(Double latitude, Double longitude) {
        locationRepository.setLastLocation(new LatLng(latitude, longitude));
    }

    // Tri par note (rating)
    public void sortRestaurantsByNote(Boolean isAscendant) {
        List<Restaurant> currentRestaurants = new ArrayList<>(restaurantRepository.getRestaurants());
        Log.d("ppp", "Current list before: " + currentRestaurants);
        if (currentRestaurants != null) {
            Collections.sort(currentRestaurants, new Comparator<Restaurant>() {
                @Override
                public int compare(Restaurant r1, Restaurant r2) {
                    if (isAscendant) {
                        return Float.compare(r2.getNote().floatValue(), r1.getNote().floatValue());

                    } else {
                        return Float.compare(r1.getNote().floatValue(), r2.getNote().floatValue());

                    }
                }
            });
            Log.d("ppp", "Sorted list after: " + currentRestaurants);

            // Met à jour les restaurants triés dans LiveData
            restaurantsLiveData.setValue(currentRestaurants);
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
                    } else {
                        return r2.getName().compareToIgnoreCase(r1.getName());
                    }
                }
            });
            // Met à jour les restaurants triés dans LiveData
            ((MutableLiveData<List<Restaurant>>) restaurantsLiveData).setValue(currentRestaurants);
        }
    }


    public void filterRestaurantsByName(String query) {
        List<Restaurant> currentRestaurants = restaurantRepository.getRestaurants();
        if (currentRestaurants != null) {
            List<Restaurant> filteredRestaurants = new ArrayList<>();
            for (Restaurant restaurant : currentRestaurants) {
                if (restaurant.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredRestaurants.add(restaurant);
                }
            }
            restaurantsLiveData.setValue(filteredRestaurants);
        }
    }


    public void filterUsersByQuery(String query) {
        List<User> currentUsers = usersLiveData.getValue();
        if (currentUsers != null) {
            List<User> filteredUsers = new ArrayList<>();
            for (User user : currentUsers) {
                if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredUsers.add(user);
                }
            }
            // Met à jour la liste filtrée dans LiveData
            ((MutableLiveData<List<User>>) usersLiveData).setValue(filteredUsers);
        }
    }

    public void initRestaurants() {
        restaurantsLiveData.postValue(restaurantRepository.getRestaurants());
    }

    public void initUsers() {
        usersLiveData = userRepository.getUsersLiveData();
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

    public String getIsRestaurantSelected() {
        User currentUser = getCurrentUser();

        String restaurantID = (String) currentUser.getSelectedRestaurant().get("id");
        Timestamp restaurantDate = (Timestamp) currentUser.getSelectedRestaurant().get("date");

        Date date = restaurantDate.toDate();
        LocalDateTime selectedDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDate selectedDate = selectedDateTime.toLocalDate();
        LocalDateTime limitDateTime = LocalDateTime.of(selectedDate, LocalTime.of(14, 0));
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(limitDateTime) || Objects.equals(restaurantID, "")) {
            return "";
        }
        return restaurantID;
    }

    public User getCurrentUser() {
        List<User> userList = userRepository.getUsersLiveData().getValue();
        String currentUserID = userRepository.getCurrentUID();
        for (User user : userList) {
            if (user.getId().equals(currentUserID)) {
                return user;
            }
        }
        return null;
    }


    public static class Factory implements ViewModelProvider.Factory {
        private final RestaurantRepository restaurantRepository;
        private final UserRepository userRepository;
        private final LocationRepository locationRepository;
        private final FirebaseApiService firebaseApiService;

        @Inject
        public Factory(RestaurantRepository restaurantRepository, UserRepository userRepository, LocationRepository locationRepository, FirebaseApiService firebaseApiService) {
            this.restaurantRepository = restaurantRepository;
            this.userRepository = userRepository;
            this.locationRepository = locationRepository;
            this.firebaseApiService = firebaseApiService;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(HomeViewModel.class)) {
                return (T) new HomeViewModel(restaurantRepository, userRepository, locationRepository, firebaseApiService);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}