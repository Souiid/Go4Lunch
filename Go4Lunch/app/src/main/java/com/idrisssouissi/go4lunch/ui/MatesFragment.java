package com.idrisssouissi.go4lunch.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idrisssouissi.go4lunch.Go4Lunch;
import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.databinding.FragmentListBinding;
import com.idrisssouissi.go4lunch.databinding.FragmentMatesBinding;

import java.util.List;


public class MatesFragment extends Fragment {

    FragmentMatesBinding binding;
    FirebaseApiService firebaseApiService;
    HomeViewModel viewModel;

    public static MatesFragment newInstance(String param1, String param2) {
        MatesFragment fragment = new MatesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseApiService = new FirebaseApiService();
        HomeViewModel.Factory factory = Go4Lunch.getAppComponent().provideHometViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMatesBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<User> userList = viewModel.usersLiveData.getValue();
        List<Restaurant> restaurantList = viewModel.restaurantsLiveData.getValue();

        //TODO: Est ce que je mets le nom du restaurant et je le passe en paramètre dans l'adapter,
        // le user ou je gére le nom dans la class de l'adapter

        binding.recyclerView.setAdapter(new UserAdapter(userList));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}