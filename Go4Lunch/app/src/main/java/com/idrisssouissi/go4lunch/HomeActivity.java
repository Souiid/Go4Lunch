package com.idrisssouissi.go4lunch;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.idrisssouissi.go4lunch.databinding.ActivityHomeBinding;

import com.idrisssouissi.go4lunch.MapFragment;
import com.idrisssouissi.go4lunch.ListFragment;
import com.idrisssouissi.go4lunch.MatesFragment;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class HomeActivity extends AppCompatActivity {

    private static final int NAVIGATION_MAP = R.id.navigation_map;
    private static final int NAVIGATION_LIST = R.id.navigation_list;
    private static final int NAVIGATION_MATES = R.id.navigation_mates;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


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
}
