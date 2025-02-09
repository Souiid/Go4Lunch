package com.idrisssouissi.go4lunch.ui;

import androidx.annotation.NonNull;
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
        assert userList != null;
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


    public Boolean getIsRestaurantLiked(String restaurantId) {
        User currentUser = getCurrentUser();
        return (currentUser.getRestaurantLikeIDs().contains(restaurantId));
    }

    public boolean getIsRestaurantSelected(String restaurantId) {
        User currentUser = getCurrentUser();
        boolean isRestaurantSelected = Objects.equals(currentUser.getSelectedRestaurant().get("id"), restaurantId);
        boolean isRestaurantValid = false;

        LocalDateTime now = LocalDateTime.now();
        LocalTime limitTime = LocalTime.of(15, 0);

        if (isRestaurantSelected) {
            Timestamp timestamp = (Timestamp) currentUser.getSelectedRestaurant().get("date");
            if (timestamp != null) {
                LocalDateTime selectionDateTime = timestamp.toDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                LocalDate selectionDate = selectionDateTime.toLocalDate();
                LocalTime selectionTime = selectionDateTime.toLocalTime();

                if (selectionDate.isEqual(now.toLocalDate())) {
                    if (selectionTime.isBefore(limitTime)) {
                        isRestaurantValid = true;
                    }
                } else if (selectionDate.isEqual(now.toLocalDate().minusDays(1))) {
                    if (selectionTime.isAfter(limitTime)) {
                        isRestaurantValid = true;
                    }
                }
            }
        }
        return isRestaurantSelected && isRestaurantValid;
    }


    public List<UserItem> getUsersByRestaurantID(String restaurantId) {
        ArrayList<UserItem> usersInRestaurant = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalTime limitTime = LocalTime.of(15, 0);

        for (User user : Objects.requireNonNull(userRepository.getUsersLiveData().getValue())) {
            if (Objects.equals(user.getSelectedRestaurant().get("id"), restaurantId)) {
                Timestamp timestamp = (Timestamp) user.getSelectedRestaurant().get("date");
                if (timestamp != null) {
                    LocalDateTime selectionDateTime = timestamp.toDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    LocalDate selectionDate = selectionDateTime.toLocalDate();
                    LocalTime selectionTime = selectionDateTime.toLocalTime();

                    if (selectionDate.isEqual(now.toLocalDate())) {
                        if (selectionTime.isBefore(limitTime)) {
                            usersInRestaurant.add(new UserItem(user.getId(), user.getName(), "", user.getPhotoUrl()));
                        }
                    } else if (selectionDate.isEqual(now.toLocalDate().minusDays(1))) {
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
        public Factory(RestaurantRepository restaurantRepository, UserRepository userRepository) {
            this.restaurantRepository = restaurantRepository;
            this.userRepository = userRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(RestaurantDetailsViewModel.class)) {
                return (T) new RestaurantDetailsViewModel(restaurantRepository, userRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
