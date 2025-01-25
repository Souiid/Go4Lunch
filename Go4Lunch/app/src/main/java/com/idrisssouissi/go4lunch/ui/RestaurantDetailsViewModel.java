package com.idrisssouissi.go4lunch.ui;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.Timestamp;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.RestaurantRepository;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.data.UserItem;
import com.idrisssouissi.go4lunch.data.UserRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;

import kotlin.Triple;

public class RestaurantDetailsViewModel extends ViewModel {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Inject
    public RestaurantDetailsViewModel(RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public Restaurant getRestaurant(String restaurantId) {
        return restaurantRepository.getRestaurantById(restaurantId);
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

    public Triple<String, String, String> getWebsiteAndPhoneNumber(String restaurantId) throws IOException {
       return restaurantRepository.getRestaurantContact(restaurantId);
    }

    public Boolean getIsRestaurantSelected(String restaurantId) {
        User currentUser = getCurrentUser();
        return (currentUser.getSelectedRestaurant().get("id").equals(restaurantId));
    }

    public Boolean getIsRestaurantLiked(String restaurantId) {
        User currentUser = getCurrentUser();
        return (currentUser.getRestaurantLikeIDs().contains(restaurantId));
    }

    public List<UserItem> getUsersByRestaurantID(String restaurantId) {
        ArrayList<UserItem> usersInRestaurant = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalTime limitTime = LocalTime.of(15, 0);

        for (User user : Objects.requireNonNull(userRepository.getUsersLiveData().getValue())) {
            if (Objects.equals(user.getSelectedRestaurant().get("id"), restaurantId)) {
                // Récupérer la date/heure de sélection comme Firebase Timestamp
                Timestamp timestamp = (Timestamp) user.getSelectedRestaurant().get("date");
                if (timestamp != null) {
                    LocalDateTime selectionDateTime = timestamp.toDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime(); // Conversion du timestamp

                    LocalDate selectionDate = selectionDateTime.toLocalDate(); // Date de sélection
                    LocalTime selectionTime = selectionDateTime.toLocalTime(); // Heure de sélection

                    if (selectionDate.isEqual(now.toLocalDate())) {
                        // Aujourd'hui
                        if (selectionTime.isBefore(limitTime)) {
                            usersInRestaurant.add(new UserItem(user.getId(), user.getName(), "", user.getPhotoUrl()));
                        }
                    } else if (selectionDate.isEqual(now.toLocalDate().minusDays(1))) {
                        // Hier
                        if (selectionTime.isAfter(limitTime)) {
                            usersInRestaurant.add(new UserItem(user.getId(), user.getName(), "", user.getPhotoUrl()));
                        }
                    }
                }
            }
        }
        return usersInRestaurant;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final RestaurantRepository restaurantRepository;
        private final UserRepository userRepository;

        @Inject
        public Factory(RestaurantRepository restaurantRepository, UserRepository userRepository) {  // Correction ici
            this.restaurantRepository = restaurantRepository;
            this.userRepository = userRepository;  // Correction ici
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(RestaurantDetailsViewModel.class)) {
                return (T) new RestaurantDetailsViewModel(restaurantRepository, userRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
