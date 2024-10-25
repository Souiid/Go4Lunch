package com.idrisssouissi.go4lunch.data;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import javax.inject.Inject;

public class UserRepository {

    private MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private FirebaseApiService firebaseApiService = new FirebaseApiService();
    private final FirebaseAuth auth;

    String currentUID;

    @Inject
    public UserRepository(FirebaseApiService firebaseApiService) {
        this.firebaseApiService = firebaseApiService;
        this.auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e("UserRepository", "Aucun utilisateur connecté.");
            // Gérez ce cas (soit en lançant une exception ou en traitant le cas d'utilisateur déconnecté)
            return;
        }
        currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getCurrentUID() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            Log.e("UserRepository", "Utilisateur non connecté lors de l'accès à l'UID.");
            return null; // ou lancez une exception, selon la logique de l'application
        }
    }

    public MutableLiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public void getAllUsers() {
        firebaseApiService.getAllUsers(users -> {
            usersLiveData.postValue(users);
        });
    }
}
