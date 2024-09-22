package com.idrisssouissi.go4lunch.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.databinding.RestaurantItemBinding;
import com.idrisssouissi.go4lunch.databinding.UserItemBinding;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UserItemBinding binding = UserItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = userList.get(position);
        holder.bind(currentUser);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private UserItemBinding binding;

        public UserViewHolder(UserItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        public void bind(User user) {

            binding.userInfoTV.setText(user.getName());
            Glide.with(binding.getRoot().getContext())
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.pic)
                    .into(binding.userIV);
        }
    }
}