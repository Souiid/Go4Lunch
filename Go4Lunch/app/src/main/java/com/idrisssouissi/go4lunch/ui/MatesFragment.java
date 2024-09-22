package com.idrisssouissi.go4lunch.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idrisssouissi.go4lunch.R;
import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.databinding.FragmentListBinding;
import com.idrisssouissi.go4lunch.databinding.FragmentMatesBinding;


public class MatesFragment extends Fragment {

    FragmentMatesBinding binding;
    FirebaseApiService firebaseApiService;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMatesBinding.inflate(getLayoutInflater(), container, false);
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseApiService.getAllUsers(users -> {
            binding.recyclerView.setAdapter(new UserAdapter(users));
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        });

    }
}