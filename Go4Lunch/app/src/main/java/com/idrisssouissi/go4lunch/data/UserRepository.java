package com.idrisssouissi.go4lunch.data;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import javax.inject.Inject;

public class UserRepository {

    private MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private FirebaseApiService firebaseApiService = new FirebaseApiService();
    String currentUID;

    @Inject
    public UserRepository(FirebaseApiService firebaseApiService) {
        this.firebaseApiService = firebaseApiService;
        currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getCurrentUID() {
        return currentUID;
    }

    public MutableLiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public void getAllUsers() {
        Log.d("aaa", "Fetching all users");
        firebaseApiService.getAllUsers(users -> {
            Log.d("aaa", "Fetched users in repo" + users.size() + " users");
            usersLiveData.postValue(users);
        });
    }
}
