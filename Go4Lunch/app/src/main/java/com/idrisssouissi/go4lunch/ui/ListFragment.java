package com.idrisssouissi.go4lunch.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.databinding.FragmentListBinding;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListFragment extends Fragment implements RestaurantAdapter.OnRestaurantClickListener, SearchView.OnQueryTextListener {

    HomeViewModel viewModel;

    private FragmentListBinding binding;
    private LatLng lastLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
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

        RestaurantAdapter adapter = new RestaurantAdapter(new ArrayList<>(), this, viewModel, requireContext());
        binding.recyclerView.setAdapter(adapter);

        viewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            binding.noRestaurantFoundTV.setVisibility(View.INVISIBLE);
            binding.recyclerView.setVisibility(View.VISIBLE);
            Log.d("ppp", "Restaurants changed: " + restaurants);
            if (restaurants == null || restaurants.isEmpty()) {
                binding.noRestaurantFoundTV.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.GONE);
                return;
            }
            for (Restaurant restaurant : restaurants) {
                restaurant.setNote(0);
                restaurant.setNumberOfUsers(0);

                Float distance = viewModel.getDistance(lastLocation, new LatLng(restaurant.getLatitude(), restaurant.getLongitude()));
                restaurant.setDistance(distance);
            }
            List<User> users = viewModel.usersLiveData.getValue();

            for (Restaurant restaurant : restaurants) {
                assert users != null;
                for (User user : users) {
                    if (user.getRestaurantLikeIDs().contains(restaurant.getId())) {
                        int restaurantNote = restaurant.getNote().intValue();
                        restaurant.setNote(restaurantNote + 1);
                    }

                    Timestamp timestamp = (Timestamp) user.getSelectedRestaurant().get("date");
                    boolean respectsConditions = false;

                    if (timestamp != null) {
                        LocalDateTime selectionDateTime = timestamp.toDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        LocalDate selectionDate = selectionDateTime.toLocalDate();
                        LocalTime selectionTime = selectionDateTime.toLocalTime();

                        LocalDate today = LocalDate.now();
                        //TODO: Changer heure ici
                        LocalTime limitTime = LocalTime.of(15, 0);

                        if (selectionDate.isEqual(today)) {
                            if (selectionTime.isBefore(limitTime)) {
                                respectsConditions = true;
                            }
                        } else if (selectionDate.isEqual(today.minusDays(1))) {
                            if (selectionTime.isAfter(limitTime)) {
                                respectsConditions = true;
                            }
                        }
                    }

                    if (respectsConditions && Objects.equals(user.getSelectedRestaurant().get("id"), restaurant.getId())) {
                        int numberOfUsers = restaurant.getNumberOfUsers().intValue();
                        restaurant.setNumberOfUsers(numberOfUsers + 1);
                    }
                }
            }

            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
            adapter.updateRestaurants(restaurants);
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        });

    }


    @Override
    public void onRestaurantClick(String restaurantID) {
        Intent intent = new Intent(getActivity(), RestaurantDetailsActivity.class);
        intent.putExtra("restaurantID", restaurantID);
        startActivity(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        viewModel.filterRestaurantsByName(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}