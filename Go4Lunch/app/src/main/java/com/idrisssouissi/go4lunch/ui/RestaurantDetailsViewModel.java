package com.idrisssouissi.go4lunch.ui;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.RestaurantRepository;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.data.UserItem;
import com.idrisssouissi.go4lunch.data.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;

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

    public void getWebsiteAndPhoneNumber(String restaurantId, Consumer<Optional<String>> website, Consumer<Optional<String>> phoneNumber) {
        restaurantRepository.getRestaurantContact(restaurantId, website, phoneNumber, null);

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
        for (User user : Objects.requireNonNull(userRepository.getUsersLiveData().getValue())) {
            if (Objects.equals(user.getSelectedRestaurant().get("id"), restaurantId)) {
                usersInRestaurant.add(new UserItem(user.getId(), user.getName(), "", user.getPhotoUrl()));
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
