package com.idrisssouissi.go4lunch.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.idrisssouissi.go4lunch.Go4Lunch;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.databinding.FragmentListBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ListFragment extends Fragment implements RestaurantAdapter.OnRestaurantClickListener{

    HomeViewModel viewModel;

    private FragmentListBinding binding;
    private LatLng lastLocation;

    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        HomeViewModel.Factory factory = Go4Lunch.getAppComponent().provideHometViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(getLayoutInflater(), container, false);
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getLastLocation().observe(getViewLifecycleOwner(), lastLocation -> {
            if (lastLocation != null) {
                this.lastLocation = lastLocation;
            }
        });
        binding.recyclerView.setVisibility(View.INVISIBLE);
        binding.noRestaurantFoundTV.setVisibility(View.INVISIBLE);
        viewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {

            if (restaurants == null || restaurants.isEmpty()) {
                binding.noRestaurantFoundTV.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            if (restaurants != null) {

                for (Restaurant restaurant : restaurants) {
                    Float distance = viewModel.getDistance(lastLocation, new LatLng(restaurant.getLatitude(), restaurant.getLongitude()));
                    restaurant.setDistance(distance);
                }

                List<User> users = viewModel.usersLiveData.getValue();

                for (Restaurant restaurant : restaurants) {
                    assert users != null;
                    for (User user : users) {
                        Log.d("aaa", "GetRestaurantLikeIDs: " + user.getRestaurantLikeIDs());
                        if(user.getRestaurantLikeIDs().contains(restaurant.getId())) {
                            Integer restaurantNote = restaurant.getNote().intValue();
                            Log.d("fff", "RESTAURANT NOTE" + restaurant.getId() +  "AVANT: " + restaurantNote);
                            restaurant.setNote(restaurantNote + 1);
                            Log.d("fff", "RESTAURANT NOTE"  + restaurant.getId() + "APRES SET: "   + restaurant.getNote().intValue());                        }
                    }
                }

                for (Restaurant restaurant : restaurants) {
                    Log.d("finalCheck", "Final Restaurant Note: " + restaurant.getId() + ": " + restaurant.getNote());
                }


                RestaurantAdapter adapter = new RestaurantAdapter(restaurants, this, viewModel);
                binding.recyclerView.setAdapter(adapter);
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
                adapter.updateRestaurants(restaurants);
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);

            }
        });

    }


    @Override
    public void onRestaurantClick(String restaurantID) {
        Intent intent = new Intent(getActivity(), RestaurantDetailsActivity.class);
        intent.putExtra("restaurantID", restaurantID);
        startActivity(intent);
    }
}