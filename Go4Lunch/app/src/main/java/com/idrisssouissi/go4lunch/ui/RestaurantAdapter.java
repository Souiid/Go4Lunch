package com.idrisssouissi.go4lunch.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.databinding.RestaurantItemBinding;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private List<Restaurant> restaurantList;
    private final OnRestaurantClickListener listener;

    public RestaurantAdapter(List<Restaurant> restaurantList, OnRestaurantClickListener listener) {
        this.restaurantList = restaurantList;
        this.listener = listener;
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
        holder.bind(currentRestaurant);
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
        public void bind(Restaurant restaurant) {
            binding.restaurantNameTV.setText(restaurant.getName());
            binding.restaurantInfoTV.setText(restaurant.getAddress());

            if (restaurant.getDistance().isPresent()) {
                binding.distanceTV.setText(restaurant.getDistance().get());
            }
            binding.hourlyTV.setText(restaurant.getOpenHours());

            Glide.with(binding.getRoot().getContext())
                    .load(restaurant.getPhotoUrl())  // Assurez-vous que getPhotoUrl() renvoie la bonne URL de la photo
                    .placeholder(R.drawable.pic)
                    .into(binding.restaurantIV);
        }
    }

    public interface OnRestaurantClickListener {
        void onRestaurantClick(String restaurantID);
    }
}