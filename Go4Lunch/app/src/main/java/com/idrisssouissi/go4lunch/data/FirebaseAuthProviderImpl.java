package com.idrisssouissi.go4lunch.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthProviderImpl implements FirebaseAuthProvider {

    @Override
    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
