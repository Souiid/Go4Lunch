package com.idrisssouissi.go4lunch.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
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

    public FirebaseFirestore db;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Inject
    public FirebaseApiService() {
        db = FirebaseFirestore.getInstance();
    }

    // 🔹 Ajout du constructeur permettant d'injecter Firestore
    public FirebaseApiService(FirebaseFirestore db) {
        this.db = db;
    }

    public void createUserInFirestore(FirebaseUser user, Runnable completion) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getDisplayName());
        userData.put("selectedRestaurant", new HashMap<String, Object>());
        userData.put("restaurantLikeIDs", new ArrayList<String>());
        userData.put("photoUrl", Objects.requireNonNull(user.getPhotoUrl()).toString());
        userData.put("email", user.getEmail());


        db.collection("users").document(user.getUid())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> completion.run());
    }

    public void getAllUsers(Consumer<List<User>> completion) {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> userList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String email = document.getString("email");
                            String name = document.getString("name");
                            String photoUrl = document.getString("photoUrl");
                            Map<String, Object> selectedRestaurantMap = (Map<String, Object>) document.get("selectedRestaurant");
                            List<String> restaurantLikeIDs = (List<String>) document.get("restaurantLikeIDs");

                            User user = new User(id, email, name, photoUrl, selectedRestaurantMap, restaurantLikeIDs);
                            userList.add(user);
                        }
                        completion.accept(userList);
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
                    NotificationScheduler.scheduleNotification(context);})
                .addOnFailureListener(e -> {});
    }

    public void updateRestaurantLikes(String restaurantId, Boolean isLiked) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference userDocRef = db.collection("users").document(userId);

        if (isLiked) {
            userDocRef.update("restaurantLikeIDs", FieldValue.arrayUnion(restaurantId))
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(e -> {});
        } else {
            userDocRef.update("restaurantLikeIDs", FieldValue.arrayRemove(restaurantId))
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(e -> {});
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
                                userNames.add(userName);
                            }
                        }
                        completion.accept(userNames);
                    } else {
                        completion.accept(new ArrayList<>());
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
