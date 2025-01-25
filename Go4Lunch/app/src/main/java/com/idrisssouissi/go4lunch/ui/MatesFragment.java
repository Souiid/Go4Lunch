package com.idrisssouissi.go4lunch.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.idrisssouissi.go4lunch.Go4Lunch;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.data.UserItem;
import com.idrisssouissi.go4lunch.databinding.FragmentMatesBinding;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import kotlin.Triple;


public class MatesFragment extends Fragment {

    FragmentMatesBinding binding;
    FirebaseApiService firebaseApiService;
    HomeViewModel viewModel;
    List<UserItem> userItemList = new ArrayList<>();
    UserAdapter adapter;

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
    public void onResume() {
        super.onResume();
        // Aucun autre code n'est nécessaire ici
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new UserAdapter(userItemList, requireContext(), false);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.recyclerView.setVisibility(View.INVISIBLE);
        binding.noUsersFoundTV.setVisibility(View.INVISIBLE);
        viewModel.uiStateLiveData.observe(getViewLifecycleOwner(), pair -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                LocalDateTime now = LocalDateTime.now();
                LocalTime limitTime = LocalTime.of(15, 0);

                List<Restaurant> restaurantList = pair.first;
                List<User> userList = pair.second;
                if (userList == null || userList.isEmpty()) {
                    binding.noUsersFoundTV.setVisibility(View.INVISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (restaurantList != null && userList != null) {
                    Map<String, String> restaurantMap = new HashMap<>();
                    for (Restaurant restaurant : restaurantList) {
                        restaurantMap.put(restaurant.getId(), restaurant.getName());
                    }

                    List<UserItem> newUserItemList = new ArrayList<>();
                    for (User user : userList) {
                        String restaurantName = "";
                        String selectedRestaurantID = user.getSelectedRestaurantID();
                        Timestamp timestamp = (Timestamp) user.getSelectedRestaurant().get("date");
                        LocalDateTime selectionDateTime = timestamp.toDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        LocalDate selectionDate = selectionDateTime.toLocalDate();
                        LocalTime selectionTime = selectionDateTime.toLocalTime();
                        LocalDate today = LocalDate.now();
                        boolean respectsConditions = false;

                        if (selectionDate.isEqual(today)) {
                            // Aujourd'hui
                            if (selectionTime.isBefore(limitTime)) {
                                respectsConditions = true;
                            }
                        } else if (selectionDate.isEqual(today.minusDays(1))) {
                            // Hier
                            if (selectionTime.isAfter(limitTime)) {
                                respectsConditions = true;
                            }
                        }


                        if (selectedRestaurantID != null && restaurantMap.containsKey(selectedRestaurantID) && respectsConditions) {
                            restaurantName = restaurantMap.get(selectedRestaurantID);
                            UserItem userItem = new UserItem(user.getId(), user.getName(), restaurantName, user.getPhotoUrl());
                            newUserItemList.add(userItem);
                        } else if (selectedRestaurantID != null && !restaurantMap.containsKey(selectedRestaurantID) && !selectedRestaurantID.isEmpty() && respectsConditions) {
                            try {
                                Triple<String, String, String> details = viewModel.getDistantRestaurantName(selectedRestaurantID);
                                restaurantName = details.component1();
                                UserItem userItem = new UserItem(user.getId(), user.getName(), restaurantName, user.getPhotoUrl());
                                newUserItemList.add(userItem);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            UserItem userItem = new UserItem(user.getId(), user.getName(), restaurantName, user.getPhotoUrl());
                            newUserItemList.add(userItem);
                        }

                        Log.d("Debug", "User: " + user.getName());
                        Log.d("Debug", "Selected Restaurant ID: " + selectedRestaurantID);
                        Log.d("Debug", "Restaurant Name: " + restaurantName);
                        Log.d("Debug", "Respects Conditions: " + respectsConditions);
                    }

                    requireActivity().runOnUiThread(() -> {
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                        userItemList.clear();
                        userItemList.addAll(newUserItemList);
                        adapter.notifyDataSetChanged();
                    });
                }
            });
        });
    }
}