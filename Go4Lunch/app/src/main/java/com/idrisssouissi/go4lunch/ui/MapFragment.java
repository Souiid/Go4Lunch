package com.idrisssouissi.go4lunch.ui;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.idrisssouissi.go4lunch.BuildConfig;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.databinding.FragmentMapBinding;

import java.io.IOException;
import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback, SearchView.OnQueryTextListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FragmentMapBinding binding;
    private static final String API_KEY = BuildConfig.GOOGLE_API_KEY;
    private Double latitude;
    private Double longitude;
    HomeViewModel viewModel;
    LiveData<List<Restaurant>> restaurantsLiveData;

    private OnRestaurantSelectedListener callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (OnRestaurantSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context
                    + " must implement OnRestaurantSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getActivity() != null;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMapBinding.inflate(getLayoutInflater(), container, false);
        assert getActivity() != null;
        Places.initialize(getActivity().getApplicationContext(), API_KEY);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        clickOnSearchButton();



        viewModel.uiStateLiveData.observe(getViewLifecycleOwner(), pair -> {
            List<User> users = pair.second;
            List<Restaurant> restaurants = pair.first;

            if (users != null && restaurants != null) {
                if (mMap != null) {
                    mMap.clear();
                    List<String> selectedRestaurantIDs = viewModel.getAllSelectedRestaurantID(users, restaurants);
                    for (Restaurant restaurant : restaurants) {
                        LatLng location = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                        MarkerOptions markerOptions = (new MarkerOptions()
                                .position(location)
                                .title(restaurant.getName())
                                .snippet(restaurant.getId()));

                        if (selectedRestaurantIDs.contains(restaurant.getId())) {
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        }
                        mMap.addMarker(markerOptions);
                    }

                    mMap.setOnMarkerClickListener(marker -> {
                        String restaurantID = marker.getSnippet();
                        if (restaurantID != null) {
                            callback.onRestaurantSelected(restaurantID);
                        }
                        return false;
                    });
                }
            }

        });

        return binding.getRoot();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        assert getActivity() != null;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                        new Thread(() -> {
                            try {
                                fetchNearbyRestaurants(latitude, longitude);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            viewModel.setLastLocation(latitude, longitude);
                        }).start();

                    }
                });

        binding.searchButton.setVisibility(View.INVISIBLE);

        mMap.setOnCameraMoveListener(() -> binding.searchButton.setVisibility(View.VISIBLE));
    }

    void clickOnSearchButton() {
        binding.searchButton.setOnClickListener(v -> {
            if (mMap != null) {
                LatLng centerOfMap = mMap.getCameraPosition().target;
                double centerLatitude = centerOfMap.latitude;
                double centerLongitude = centerOfMap.longitude;
                new Thread(() -> {
                    try {

                        fetchNearbyRestaurants(centerLatitude, centerLongitude);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    v.setVisibility(View.INVISIBLE);
                }).start();
            }
        });
    }

    private void fetchNearbyRestaurants(double latitude, double longitude) throws IOException {
        restaurantsLiveData = viewModel.getRestaurantsByFetch(latitude, longitude);
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
