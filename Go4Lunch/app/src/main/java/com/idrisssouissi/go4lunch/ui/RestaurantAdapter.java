package com.idrisssouissi.go4lunch.ui;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.databinding.RestaurantItemBinding;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private List<Restaurant> restaurantList;
    private final OnRestaurantClickListener listener;
    private HomeViewModel viewModel;

    public RestaurantAdapter(List<Restaurant> restaurantList,
                             OnRestaurantClickListener listener,
                             HomeViewModel viewModel) {
        this.restaurantList = new ArrayList<>(restaurantList);
        this.listener = listener;
        this.viewModel = viewModel;
    }

    public void updateRestaurants(List<Restaurant> updatedRestaurants) {
        Log.d("RestaurantAdapter", "Updated restaurants: " + updatedRestaurants.size());
        this.restaurantList = new ArrayList<>(updatedRestaurants);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RestaurantItemBinding binding = RestaurantItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RestaurantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RestaurantViewHolder holder, int position) {
        Restaurant currentRestaurant = restaurantList.get(position);
        holder.bind(currentRestaurant, viewModel);

        holder.itemView.setOnClickListener(v -> listener.onRestaurantClick(currentRestaurant.getId()));
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private RestaurantItemBinding binding;

        public RestaurantViewHolder(RestaurantItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        public void bind(Restaurant restaurant, HomeViewModel viewModel) {
            binding.restaurantNameTV.setText(restaurant.getName());
            binding.restaurantInfoTV.setText(restaurant.getAddress());
            Log.d("ttt", "Number of users in adapter: " + restaurant.getNumberOfUsers());

            if (restaurant.getDistance().isPresent()) {
                Float distance = restaurant.getDistance().get();
                binding.distanceTV.setText(viewModel.formatDistance(distance));
            }
            LocalTime openLocalTime = restaurant.getOpenHours()[0];
            LocalTime closeLocalTime = restaurant.getOpenHours()[1];

            LocalTime now = LocalTime.now(); // Heure actuelle

            String textToDisplay = "Open until: " + closeLocalTime.toString();

            if (now.isBefore(openLocalTime)) {
                textToDisplay = "Closed now, open at " + openLocalTime.toString();
            }

            if (now.isAfter(closeLocalTime)) {
                textToDisplay = "Closed now";
            }

            Duration timeUntilClose = Duration.between(now, closeLocalTime);
            if (timeUntilClose.toMinutes() <= 30) {
                textToDisplay = "Closing soon, close at " + closeLocalTime;
            }

            binding.hourlyTV.setText(textToDisplay);
            binding.peopleCountTV.setVisibility(View.INVISIBLE);
            binding.iconPerson.setVisibility(View.INVISIBLE);
            if (restaurant.getNumberOfUsers().intValue() != 0) {
                binding.peopleCountTV.setText("(" + restaurant.getNumberOfUsers().intValue() + ")");
                binding.peopleCountTV.setVisibility(View.VISIBLE);
                binding.iconPerson.setVisibility(View.VISIBLE);
            }

            int stars = 0;
            Integer note = restaurant.getNote().intValue();

            if (note >= 2) {
                stars = 1;
            }

            if (note >= 5) {
                stars = 2;
            }

            if (note >= 8) {
                stars = 3;
            }

            binding.starContainer.removeAllViews();
            for (int i = 0; i < stars; i++) {
                ImageView star = new ImageView(binding.getRoot().getContext());
                star.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                star.setImageResource(R.drawable.ic_star);
                star.setColorFilter(ContextCompat.getColor(binding.getRoot().getContext(), R.color.yellow));
                binding.starContainer.addView(star);
            }

            Glide.with(binding.getRoot().getContext())
                    .load(restaurant.getPhotoUrl())
                    .placeholder(R.drawable.pic)
                    .into(binding.restaurantIV);
        }
    }

    public interface OnRestaurantClickListener {
        void onRestaurantClick(String restaurantID);
    }
}