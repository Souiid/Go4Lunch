package com.idrisssouissi.go4lunch.data;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.inject.Inject;

public class FirebaseApiService {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Inject
    public FirebaseApiService() {
        db = FirebaseFirestore.getInstance();
    }

    public void createUserInFirestore(FirebaseUser user, Runnable completion) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getDisplayName());
        userData.put("selectedRestaurantID", "");
        userData.put("photoUrl", user.getPhotoUrl().toString());
        userData.put("email", user.getEmail());


        db.collection("users").document(user.getUid())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> completion.run())
                .addOnFailureListener(e -> Log.w("Firestore", "Erreur lors de la création de l'utilisateur.", e));
    }

    public void createUserInFirestoreForTest(User user, Runnable completion) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("selectedRestaurantID", "");
        userData.put("photoUrl", user.getPhotoUrl().toString());
        userData.put("email", user.getEmail());

        db.collection("users").document()
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> completion.run())
                .addOnFailureListener(e -> Log.w("Firestore", "Erreur lors de la création de l'utilisateur.", e));
    }

    public void getAllUsers(Consumer<List<User>> completion) {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> userList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            // Récupération manuelle des informations
                            String id = document.getId(); // Utiliser l'ID du document
                            String email = document.getString("email");
                            String name = document.getString("name");
                            String photoUrl = document.getString("photoUrl");
                            Map<String, Object> selectedRestaurantMap = (Map<String, Object>) document.get("selectedRestaurant");

                            // Créer un objet User avec les informations récupérées
                            User user = new User(id, email, name, photoUrl, selectedRestaurantMap);
                            userList.add(user);
                        }
                        Log.d("aaa", "FIRESTORE Utilisateurs récupérés avec succès.");
                        // Exécuter l'action de fin une fois les utilisateurs récupérés
                        completion.accept(userList);
                    } else {
                        Log.w("aaa", "Erreur lors de la récupération des utilisateurs.", task.getException());
                    }
                });
    }

    public void updateSelectedRestaurant(String restaurantId) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference userDocRef = db.collection("users").document(userId);

        Map<String, Object> selectedRestaurant = new HashMap<>();
        selectedRestaurant.put("id", restaurantId);
        selectedRestaurant.put("date", Timestamp.now());

        userDocRef.update("selectedRestaurant", selectedRestaurant)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Selected restaurant updated successfully!");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error updating document: " + e.getMessage());
                });
    }
}