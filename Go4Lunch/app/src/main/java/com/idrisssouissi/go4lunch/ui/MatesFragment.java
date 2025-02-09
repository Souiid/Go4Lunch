package com.idrisssouissi.go4lunch.ui;

import android.annotation.SuppressLint;
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

    @SuppressLint("NotifyDataSetChanged")
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
                List<Restaurant> restaurantList = pair.first;
                List<User> userList = pair.second;
                if (userList == null || userList.isEmpty()) {
                    binding.noUsersFoundTV.setVisibility(View.INVISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (restaurantList != null && userList != null) {

                    List<UserItem> newUserItemList = viewModel.setUserItemList(userList, restaurantList);

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