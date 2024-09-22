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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.idrisssouissi.go4lunch.Go4Lunch;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.databinding.FragmentListBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ListFragment extends Fragment implements RestaurantAdapter.OnRestaurantClickListener{


  //  List<Restaurant> restaurantList = new ArrayList<>();
   LiveData<List<Restaurant>> restaurantsLiveData;

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
        //restaurantList.add(new Restaurant("1", "Le Gourmet", "123 Rue de Paris", 48.8566, 2.3522, String.valueOf(Optional.of("Français")), "https://example.com/photo1.jpg"));
        //restaurantList.add(new Restaurant("2", "Pasta Bella", "456 Avenue de Rome", 41.9028, 12.4964, String.valueOf(Optional.of("Italien")), "https://example.com/photo2.jpg"));
        //restaurantList.add(new Restaurant("3", "Sushi Time", "789 Rue de Tokyo", 35.6762, 139.6503, String.valueOf(Optional.of("Japonais")), "https://example.com/photo3.jpg"));
        //restaurantList.add(new Restaurant("4", "The Burger Joint", "101 Rue de New York", 40.7128, -74.0060, String.valueOf(Optional.of("Américain")), "https://example.com/photo4.jpg"));
        //restaurantList.add(new Restaurant("5", "Mystery Cuisine", "321 Avenue de l'Inconnu", 48.8566, 2.3522, String.valueOf(Optional.empty()), "https://example.com/photo5.jpg"));

        viewModel.getLastLocation().observe(getViewLifecycleOwner(), lastLocation -> {
            if (lastLocation != null) {
                this.lastLocation = lastLocation;
            }
        });



        viewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            if (restaurants != null) {

                for (Restaurant restaurant : restaurants) {
                    String distance = viewModel.getDistance(lastLocation, new LatLng(restaurant.getLatitude(), restaurant.getLongitude()));
                    restaurant.setDistance(distance);
                }

                RestaurantAdapter adapter = new RestaurantAdapter(restaurants, this);
                binding.recyclerView.setAdapter(adapter);
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
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