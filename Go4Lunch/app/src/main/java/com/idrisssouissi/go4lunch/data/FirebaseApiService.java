package com.idrisssouissi.go4lunch.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.protobuf.Any;
import com.idrisssouissi.go4lunch.NotificationScheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.inject.Inject;

public class FirebaseApiService {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Inject
    public FirebaseApiService() {
        db = FirebaseFirestore.getInstance();
    }

    public void createUserInFirestore(FirebaseUser user, Runnable completion) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getDisplayName());
        userData.put("selectedRestaurant", new HashMap<String, Object>());
        userData.put("restaurantLikeIDs", new ArrayList<String>());
        userData.put("photoUrl", user.getPhotoUrl().toString());
        userData.put("email", user.getEmail());


        db.collection("users").document(user.getUid())
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
                            String id = document.getId(); // Utiliser l'ID du document
                            String email = document.getString("email");
                            String name = document.getString("name");
                            String photoUrl = document.getString("photoUrl");
                            Map<String, Object> selectedRestaurantMap = (Map<String, Object>) document.get("selectedRestaurant");
                            List<String> restaurantLikeIDs = (List<String>) document.get("restaurantLikeIDs");

                            // Créer un objet User avec les informations récupérées
                            User user = new User(id, email, name, photoUrl, selectedRestaurantMap, restaurantLikeIDs);
                            userList.add(user);
                        }
                        // Exécuter l'action de fin une fois les utilisateurs récupérés
                        completion.accept(userList);
                    } else {
                        Log.w("aaa", "Erreur lors de la récupération des utilisateurs.", task.getException());
                    }
                });
    }

    public void updateSelectedRestaurant(String restaurantId, Boolean isSelected, Context context) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference userDocRef = db.collection("users").document(userId);

        Map<String, Object> selectedRestaurant = new HashMap<>();
        selectedRestaurant.put("id", restaurantId);
        selectedRestaurant.put("date", Timestamp.now());
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!isSelected) {
            NotificationScheduler.cancelNotification(context);
            editor.putString("restaurantID", "");
            editor.apply();
            selectedRestaurant.put("id", "");
        }

        userDocRef.update("selectedRestaurant", selectedRestaurant)
                .addOnSuccessListener(aVoid -> {
                    if (!isSelected) return;
                    editor.putString("restaurantID", restaurantId);
                    editor.apply();
                    NotificationScheduler.scheduleNotification(context);
                    System.out.println("Selected restaurant updated successfully!");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error updating document: " + e.getMessage());
                });
    }

    public void updateRestaurantLikes(String restaurantId, Boolean isLiked) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference userDocRef = db.collection("users").document(userId);

        if (isLiked) {
            // Ajouter le restaurant à la liste
            userDocRef.update("restaurantLikeIDs", FieldValue.arrayUnion(restaurantId))
                    .addOnSuccessListener(aVoid -> {
                        System.out.println("Restaurant ajouté aux likes avec succès !");
                    })
                    .addOnFailureListener(e -> {
                        System.err.println("Erreur lors de l'ajout du like : " + e.getMessage());
                    });
        } else {
            // Retirer le restaurant de la liste
            userDocRef.update("restaurantLikeIDs", FieldValue.arrayRemove(restaurantId))
                    .addOnSuccessListener(aVoid -> {
                        System.out.println("Restaurant retiré des likes avec succès !");
                    })
                    .addOnFailureListener(e -> {
                        System.err.println("Erreur lors du retrait du like : " + e.getMessage());
                    });
        }
    }

    public void getUserNamesInRestaurant(String restaurantId, Consumer<List<String>> completion) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date yesterdayAt3PM = calendar.getTime();

        db.collection("users")
                .whereEqualTo("selectedRestaurant.id", restaurantId)
                .whereGreaterThanOrEqualTo("selectedRestaurant.date", yesterdayAt3PM)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> userNames = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String userName = document.getString("name");

                            if (userName != null) {
                                //TODO: add name according to times
                                userNames.add(userName);
                            }
                        }
                        // Retourner la liste des noms des utilisateurs
                        completion.accept(userNames);
                    } else {
                        Log.w("Firestore", "Erreur lors de la récupération des utilisateurs.", task.getException());
                        completion.accept(new ArrayList<>()); // Retourner une liste vide en cas d'erreur
                    }
                });
    }

    public void signOut() {
        auth.signOut();
    }

    public Boolean isUserConnected() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null;
    }
}
