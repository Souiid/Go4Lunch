package com.idrisssouissi.go4lunch;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.idrisssouissi.go4lunch.databinding.ActivityHomeBinding;
import com.idrisssouissi.go4lunch.MapFragment;
import com.idrisssouissi.go4lunch.ListFragment;
import com.idrisssouissi.go4lunch.MatesFragment;

public class HomeActivity extends AppCompatActivity {

    private static final int NAVIGATION_MAP = R.id.navigation_map;
    private static final int NAVIGATION_LIST = R.id.navigation_list;
    private static final int NAVIGATION_MATES = R.id.navigation_mates;


    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case NAVIGATION_MAP:
                        selectedFragment = new MapFragment();
                        break;
                    case NAVIGATION_LIST:
                        selectedFragment = new ListFragment();
                        break;
                    case NAVIGATION_MATES:
                        selectedFragment = new MatesFragment();
                        break;
                }
                if (selectedFragment != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, selectedFragment);
                    transaction.commit();
                }
                return true;
            }        });

        // Pour afficher le premier fragment par défaut
        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_map);
        }
    }
}
