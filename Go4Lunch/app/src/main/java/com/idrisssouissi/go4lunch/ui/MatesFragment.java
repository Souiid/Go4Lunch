package com.idrisssouissi.go4lunch.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idrisssouissi.go4lunch.Go4Lunch;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.data.UserItem;
import com.idrisssouissi.go4lunch.databinding.FragmentListBinding;
import com.idrisssouissi.go4lunch.databinding.FragmentMatesBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MatesFragment extends Fragment {

    FragmentMatesBinding binding;
    FirebaseApiService firebaseApiService;
    HomeViewModel viewModel;

    public static MatesFragment newInstance(String param1, String param2) {
        MatesFragment fragment = new MatesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseApiService = new FirebaseApiService();
        HomeViewModel.Factory factory = Go4Lunch.getAppComponent().provideHometViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMatesBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<User> userList = viewModel.usersLiveData.getValue();
        List<Restaurant> restaurantList = viewModel.restaurantsLiveData.getValue();

        Map<String, String> restaurantMap = new HashMap<>();
        if (restaurantList != null) {
            for (Restaurant restaurant : restaurantList) {
                restaurantMap.put(restaurant.getId(), restaurant.getName());
            }
        }

        List<UserItem> userItemList = new ArrayList<>();
        if (userList != null) {
            for (User user : userList) {
                String restaurantName = "Pas encore sélectionné";
                String selectedRestaurantID = user.getSelectedRestaurantID();

                if (selectedRestaurantID != null && restaurantMap.containsKey(selectedRestaurantID)) {
                    restaurantName = restaurantMap.get(selectedRestaurantID);
                }

                UserItem userItem = new UserItem(user.getId(), user.getName(), restaurantName, user.getPhotoUrl());
                userItemList.add(userItem);
            }
        }

        binding.recyclerView.setAdapter(new UserAdapter(userItemList));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}