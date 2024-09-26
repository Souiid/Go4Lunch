package com.idrisssouissi.go4lunch.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.idrisssouissi.go4lunch.Go4Lunch;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.databinding.ActivityRestaurantDetailsBinding;

import java.util.Optional;

public class RestaurantDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PHONE = 1;
    ActivityRestaurantDetailsBinding binding;
    String restaurantID;
    private RestaurantDetailsViewModel viewModel;
    private Restaurant restaurant;
    FirebaseApiService firebaseApiService = new FirebaseApiService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRestaurantDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RestaurantDetailsViewModel.Factory factory = Go4Lunch.getAppComponent().provideRestaurantDetailsViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(RestaurantDetailsViewModel.class);
        restaurantID = getIntent().getStringExtra("restaurantID");
        restaurant = viewModel.getRestaurant(restaurantID);

        viewModel.getWebsiteAndPhoneNumber(restaurantID, this::clickOnWebsiteButton, this::clickOnPhoneButton);

        binding.restaurantName.setText(restaurant.getName());
        binding.restaurantAddress.setText(restaurant.getAddress());

        Glide.with(binding.getRoot().getContext())
                .load(restaurant.getPhotoUrl())
                .placeholder(R.drawable.pic)
                .into(binding.restaurantImage);

       Boolean isRestaurantSelected = viewModel.getIsRestaurantSelected(restaurantID);

        if (isRestaurantSelected) {
            binding.participationButton.setImageTintList(getResources().getColorStateList(R.color.green));
        }

        clickOnBackButton();
        clickOnLikeButton();
        clickOnParticipationButton();
    }

    private void clickOnBackButton() {
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    public void clickOnPhoneButton(Optional<String> phoneNumber) {
        binding.phoneButton.setOnClickListener(v -> {
            if (phoneNumber != null && phoneNumber.isPresent()) {
                String phone = phoneNumber.get();

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
                } else {
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
                }
            } else {
                Toast.makeText(this, "No phone number found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clickOnLikeButton() {
        binding.likeButton.setOnClickListener(v -> {

        });
    }

    private void clickOnWebsiteButton(Optional<String> website) {
        binding.websiteButton.setOnClickListener(v -> {
            if (website != null && website.isPresent()) {
                String url = website.get();
                Log.d("ooo", "URL: " + url);
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "No application can handle this request. Please install a web browser.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "No website found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void clickOnParticipationButton() {
        binding.participationButton.setOnClickListener(v -> {
            binding.participationButton.setImageTintList(getResources().getColorStateList(R.color.green));
            firebaseApiService.updateSelectedRestaurant(restaurantID);
        });
    }
}
