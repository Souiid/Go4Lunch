package com.idrisssouissi.go4lunch.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.idrisssouissi.go4lunch.Go4Lunch;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.RestaurantRepository;
import com.idrisssouissi.go4lunch.databinding.ActivityHomeBinding;


import javax.inject.Inject;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class HomeActivity extends AppCompatActivity implements OnRestaurantSelectedListener  {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    ActivityHomeBinding binding;
    private HomeViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        HomeViewModel.Factory factory = Go4Lunch.getAppComponent().provideHometViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        Go4Lunch.getAppComponent().inject(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        // Configure the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Glide.with(this)
                .load(R.drawable.pic)
                .transform(new CenterCrop(), new BlurTransformation(25))
                .into((ImageView) binding.navView.getHeaderView(0).findViewById(R.id.headerIV));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_lunch:
                        // Handle the lunch action
                        break;
                    case R.id.nav_settings:
                        // Handle the settings action
                        break;
                    case R.id.nav_logout:
                        // Handle the logout action
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_map:
                        selectedFragment = new MapFragment();
                        break;
                    case R.id.navigation_list:
                        selectedFragment = new ListFragment();
                        break;
                    case R.id.navigation_mates:
                        selectedFragment = new MatesFragment();
                        break;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            }
        });

        // Pour afficher le premier fragment par défaut
        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_map);
        }


    }

    @Override
    public void onRestaurantSelected(String restaurantID) {
        Intent intent = new Intent(this, RestaurantDetailsActivity.class);
        intent.putExtra("restaurantID", restaurantID);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Log.d("HomeActivity", "onActivityResult called from RestaurantDetailsActivity");
            viewModel.refreshUsers();
        }
    }
}
