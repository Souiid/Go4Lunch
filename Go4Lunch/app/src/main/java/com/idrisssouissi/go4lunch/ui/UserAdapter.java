package com.idrisssouissi.go4lunch.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.data.UserItem;
import com.idrisssouissi.go4lunch.databinding.UserItemBinding;

import java.util.List;
import java.util.Objects;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserItem> userList;
    private Context context;
    private Boolean isDetails;

    public UserAdapter(List<UserItem> userList, Context context, Boolean isDetails) {
        this.userList = userList;
        this.context = context;
        this.isDetails = isDetails;
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UserItemBinding binding = UserItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding, context, isDetails);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserItem currentUser = userList.get(position);
        holder.bind(currentUser);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private UserItemBinding binding;
        private Context context;
        private Boolean isDetails;

        public UserViewHolder(UserItemBinding binding, Context context, Boolean isDetails) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = context;
            this.isDetails = isDetails;
        }

        @SuppressLint("SetTextI18n")
        public void bind(UserItem user) {
            if (!isDetails) {
                if (!Objects.equals(user.getRestaurantName(), "")) {
                    binding.userInfoTV.setText(user.getName() + context.getString(R.string.to) + user.getRestaurantName());
                }else {
                    binding.userInfoTV.setText(user.getName() + " " + context.getString(R.string.has_not_yet_selected));
                }
            }else {
                binding.userInfoTV.setText(user.getName());
            }

            Glide.with(binding.getRoot().getContext())
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.pic)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.userIV);
        }
    }
}
