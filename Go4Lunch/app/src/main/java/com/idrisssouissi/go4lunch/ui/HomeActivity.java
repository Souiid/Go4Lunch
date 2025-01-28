package com.idrisssouissi.go4lunch.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.idrisssouissi.go4lunch.Go4Lunch;
import com.idrisssouissi.go4lunch.NotificationScheduler;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.SettingsActivity;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.databinding.ActivityHomeBinding;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Objects;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class HomeActivity extends AppCompatActivity implements OnRestaurantSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    ActivityHomeBinding binding;
    private HomeViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NotificationScheduler.scheduleNotification(this);
        //Test


        HomeViewModel.Factory factory = Go4Lunch.getAppComponent().provideHometViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        Log.d("ppp", "Activity ViewModel instance: " + viewModel);
        Go4Lunch.getAppComponent().inject(this);

        viewModel.getUserConnectionStatus().observe(this, isConnected -> {
            if (isConnected != null && !isConnected) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

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

        TextView nameTV = binding.navView.getHeaderView(0).findViewById(R.id.nameTV);
        TextView mailTV = binding.navView.getHeaderView(0).findViewById(R.id.mailTV);
        CircularImageView profileIV = binding.navView.getHeaderView(0).findViewById(R.id.profileIV);

        viewModel.usersLiveData.observe(this, users -> {
            if (!users.isEmpty()) {
               User currentUser = viewModel.getCurrentUser();
               nameTV.setText(currentUser.getName());
               mailTV.setText(currentUser.getEmail());
               Glide.with(this)
                       .load(currentUser.getPhotoUrl())
                       .circleCrop()
                       .into(profileIV);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_lunch:
                        String restaurantID = viewModel.getIsRestaurantSelected();
                        if (!Objects.equals(restaurantID, "")) {
                            Intent intent = new Intent(HomeActivity.this, RestaurantDetailsActivity.class);
                            intent.putExtra("restaurantID", restaurantID);
                            startActivityForResult(intent, 1);
                        } else {
                            Toast.makeText(HomeActivity.this, getString(R.string.you_dont_selected_restaurant), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.nav_settings:
                        Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_logout:
                        viewModel.signOut();
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

                    // Ajoutez cette ligne pour forcer la mise à jour du menu
                    invalidateOptionsMenu();
                }
                return true;
            }
        });
        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_map);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> invalidateOptionsMenu());
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        // Handle the visibility of the sort item
        MenuItem sortItem = menu.findItem(R.id.action_sort);
        MenuItem searchViewItem = menu.findItem(R.id.search_item);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        sortItem.setVisible(currentFragment instanceof ListFragment);

        searchViewItem.setVisible(!(currentFragment instanceof MatesFragment));

        // Setup the SearchView
        MenuItem searchItem = menu.findItem(R.id.search_item);
        SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setQueryHint(getString(R.string.search));
        searchView.setIconifiedByDefault(true);

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Optional: Handle the expand action
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                viewModel.initRestaurants();
                //    Toast.makeText(HomeActivity.this, "Search view closed", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof ListFragment) {
                    ((ListFragment) currentFragment).onQueryTextSubmit(query);
                } else if (currentFragment instanceof MapFragment) {
                    ((MapFragment) currentFragment).onQueryTextSubmit(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            showPopupMenu(findViewById(R.id.action_sort));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        invalidateOptionsMenu();
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
            viewModel.refreshUsers();
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        // Définir l'action à effectuer lorsqu'un élément du popup menu est sélectionné
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.sort_by_note:
                        viewModel.sortRestaurantsByNote(true);
                        return true;

                    case R.id.sort_by_note2:
                        viewModel.sortRestaurantsByNote(false);
                        return true;
                    case R.id.sort_by_distance:
                        viewModel.sortRestaurantsByDistance();
                        return true;
                    case R.id.alphabetical_order:
                        viewModel.sortRestaurantsByName(true);
                        return true;

                    case R.id.alphabetical_order2:
                        viewModel.sortRestaurantsByName(false);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }
}
