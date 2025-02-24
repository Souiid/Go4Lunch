package com.idrisssouissi.go4lunch.data;

import com.google.firebase.auth.FirebaseUser;

public interface FirebaseAuthProvider {
    FirebaseUser getCurrentUser();
    void signOut();
}
