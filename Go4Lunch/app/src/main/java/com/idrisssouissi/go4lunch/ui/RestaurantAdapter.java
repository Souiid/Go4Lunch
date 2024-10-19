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

            if (restaurant.getDistance().isPresent()) {
                Float distance = restaurant.getDistance().get();
                binding.distanceTV.setText(viewModel.formatDistance(distance));
            }
            binding.hourlyTV.setText(restaurant.getOpenHours());

            int stars = 0;
            Log.d("adapterBind", "Restaurant ID: " + restaurant.getId() + ", Note in Adapter: " + restaurant.getNote().intValue());
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

            Log.d("adapterBind", "Restaurant ID: " + restaurant.getId() + ", Calculated Stars: " + stars);

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