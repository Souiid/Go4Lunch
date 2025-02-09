package com.idrisssouissi.go4lunch.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private final HomeViewModel viewModel;
    private final Context context;


    public RestaurantAdapter(List<Restaurant> restaurantList,
                             OnRestaurantClickListener listener,
                             HomeViewModel viewModel, Context context) {
        this.restaurantList = new ArrayList<>(restaurantList);
        this.listener = listener;
        this.viewModel = viewModel;
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateRestaurants(List<Restaurant> updatedRestaurants) {
        this.restaurantList = new ArrayList<>(updatedRestaurants);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RestaurantItemBinding binding = RestaurantItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RestaurantViewHolder(binding, context);
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
        private final RestaurantItemBinding binding;
        private final Context context;

        public RestaurantViewHolder(RestaurantItemBinding binding, Context context) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = context;
        }

        @SuppressLint("SetTextI18n")
        public void bind(Restaurant restaurant, HomeViewModel viewModel) {
            binding.restaurantNameTV.setText(restaurant.getName());
            binding.restaurantInfoTV.setText(restaurant.getAddress());

            if (restaurant.getDistance().isPresent()) {
                Float distance = restaurant.getDistance().get();
                binding.distanceTV.setText(viewModel.formatDistance(distance));
            }
            LocalTime openLocalTime = restaurant.getOpenHours()[0];
            LocalTime closeLocalTime = restaurant.getOpenHours()[1];

            LocalTime now = LocalTime.now();

            String textToDisplay;

            if (openLocalTime == null || closeLocalTime == null) {
                textToDisplay = context.getString(R.string.unknow_hourlies);
            } else if (openLocalTime.equals(LocalTime.of(0, 0)) && closeLocalTime.equals(LocalTime.of(0, 0))) {
                textToDisplay = context.getString(R.string.closed_today);
            } else if (now.isBefore(openLocalTime)) {
                textToDisplay = context.getString(R.string.closed_now_open_at);
            } else if (now.isAfter(closeLocalTime)) {
                textToDisplay = context.getString(R.string.closed_now);
            } else {
                Duration timeUntilClose = Duration.between(now, closeLocalTime);
                if (timeUntilClose.toMinutes() <= 30) {
                    textToDisplay = context.getString(R.string.closing_soon, closeLocalTime.toString());
                } else {
                    textToDisplay = context.getString(R.string.open_until, closeLocalTime.toString());;
                }
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
            int note = restaurant.getNote().intValue();

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