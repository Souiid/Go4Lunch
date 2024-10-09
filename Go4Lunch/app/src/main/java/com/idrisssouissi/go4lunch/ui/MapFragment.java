package com.idrisssouissi.go4lunch.ui;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.idrisssouissi.go4lunch.Go4Lunch;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.databinding.FragmentMapBinding;
import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private  GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FragmentMapBinding binding;
    private static final String API_KEY = "AIzaSyAAraXL4skscBsmQ1z4Nt2xFszLnnajDa0";
    private Double latitude;
    private Double longitude;

    HomeViewModel viewModel;

    LiveData<List<Restaurant>> restaurantsLiveData;

    public static MapFragment newInstance() {
        return new MapFragment();
    }
    private OnRestaurantSelectedListener callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (OnRestaurantSelectedListener) context;  // Assurez-vous que l'Activity implémente l'interface
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnRestaurantSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        HomeViewModel.Factory factory = Go4Lunch.getAppComponent().provideHometViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMapBinding.inflate(getLayoutInflater(), container, false);
        Places.initialize(getActivity().getApplicationContext(), API_KEY);


        // Initialisation du fragment de la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        clickOnSearchButton();


        viewModel.liveDataMerger.observe(getViewLifecycleOwner(), pair ->{
            List<User> users = pair.second;
            List<Restaurant> restaurants = pair.first;
            Log.d("aaa", "RESTAURANTS COUNT:" + restaurants.size());
            Log.d("aaa", "USERS COUNT:" + users.size());

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
                            callback.onRestaurantSelected(restaurantID);  // Demander à `HomeActivity` de gérer cela
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

        // Permettre le zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        // Vérifiez les permissions et obtenez la localisation
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Demander les permissions si elles ne sont pas accordées
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        // Activer la localisation sur la carte
        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            fetchNearbyRestaurants(latitude, longitude);
                            viewModel.setLastLocation(latitude, longitude);
                        }
                    }
                });

        binding.searchButton.setVisibility(View.INVISIBLE);

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                binding.searchButton.setVisibility(View.VISIBLE);
            }
        });


    }

    void clickOnSearchButton() {
        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    LatLng centerOfMap = mMap.getCameraPosition().target;
                    double centerLatitude = centerOfMap.latitude;
                    double centerLongitude = centerOfMap.longitude;
                    fetchNearbyRestaurants(centerLatitude, centerLongitude);
                    v.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void fetchNearbyRestaurants(double latitude, double longitude) {
        restaurantsLiveData = viewModel.getRestaurantsByFetch(latitude, longitude);
    }



}
