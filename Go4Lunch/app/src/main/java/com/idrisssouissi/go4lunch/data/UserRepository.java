package com.idrisssouissi.go4lunch.data;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class UserRepository {

    private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private final FirebaseApiService firebaseApiService;
    private final FirebaseAuth auth;

    String currentUID;

    @Inject
    public UserRepository(FirebaseApiService firebaseApiService) {
        this.firebaseApiService = firebaseApiService;
        this.auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        currentUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    public String getCurrentUID() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }
    }

    public MutableLiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public void getAllUsers() {
        firebaseApiService.getAllUsers(usersLiveData::postValue);
    }
}
