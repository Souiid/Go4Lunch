package com.idrisssouissi.go4lunch.data;

import android.util.Log;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RestaurantApiService {

    private static final String API_KEY = "AIzaSyBig97MXmqFVmydv38OkE8d0SXxeCaTbtU";
    OkHttpClient client = new OkHttpClient();

    public void fetchNearbyRestaurants(double latitude, double longitude, Consumer<List<Restaurant>> callback) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + latitude + "," + longitude +
                "&radius=" + 500 +
                "&type=restaurant" +
                "&key=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String jsonData = response.body().string();
                    callback.accept(parseAndReturnRestaurants(jsonData));
                } else {
                    Log.e("aaa", "Request failed: " + response);
                }
            } catch (IOException e) {
                Log.e("aaa", "Request error", e);
            }
        }).start();
    }

    private List<Restaurant> parseAndReturnRestaurants(String jsonData) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
        JsonArray results = jsonObject.getAsJsonArray("results");
        ArrayList<Restaurant> restaurants = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            JsonObject restaurant = results.get(i).getAsJsonObject();
            String placeId = restaurant.get("place_id").getAsString();
            Log.d("ddd", "RESTAURANT ID: " + placeId);
            String name = restaurant.get("name").getAsString();
            String address = restaurant.get("vicinity").getAsString();
            JsonObject location = restaurant.getAsJsonObject("geometry").getAsJsonObject("location");
            double lat = location.get("lat").getAsDouble();
            double lng = location.get("lng").getAsDouble();

            // Vérification si le tableau des photos existe et contient au moins un élément
            String photoUrl = "";
            if (restaurant.has("photos") && restaurant.getAsJsonArray("photos").size() > 0) {
                photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="
                        + restaurant.getAsJsonArray("photos").get(0).getAsJsonObject().get("photo_reference").getAsString()
                        + "&key=" + API_KEY;
            }

            String openingHours = "Unknown";
            if (restaurant.has("opening_hours")) {
                JsonObject openingHoursObject = restaurant.getAsJsonObject("opening_hours");
                if (openingHoursObject.has("open_now")) {
                    boolean isOpen = openingHoursObject.get("open_now").getAsBoolean();

                    if (isOpen) {
                        if (openingHoursObject.has("periods")) {
                            JsonArray periods = openingHoursObject.getAsJsonArray("periods");
                            if (periods.size() > 0) {
                                JsonObject todayPeriod = periods.get(0).getAsJsonObject();
                                if (todayPeriod.has("close")) {
                                    JsonObject closeInfo = todayPeriod.getAsJsonObject("close");
                                    String closeTime = closeInfo.get("time").getAsString();
                                    openingHours = "Open until " + formatTime(closeTime);
                                } else {
                                    openingHours = "Open 24/24";
                                }
                            }
                        }
                    } else {
                        openingHours = "Closed Now";
                    }
                }
            }

            // Récupérer le numéro de téléphone si disponible
            Optional<String> phoneNumber = null;
            if (restaurant.has("formatted_phone_number")) {
                phoneNumber = Optional.ofNullable(restaurant.get("formatted_phone_number").getAsString());
            }

            // Récupérer le site web si disponible
            Optional<String> website = null;
            if (restaurant.has("website")) {
                website = Optional.ofNullable(restaurant.get("website").getAsString());
            }

            Restaurant restaurantToSave = new Restaurant(placeId, name, address, lat, lng, "", photoUrl, openingHours, phoneNumber, website, Optional.of(0), Optional.of(0));
            restaurants.add(restaurantToSave);
        }
        return restaurants;
    }

    void getRestaurantDetailsFromId(String restaurantId, Consumer<Optional<String>> websiteCallback, Consumer<Optional<String>> phoneCallback) {
        String url = "https://maps.googleapis.com/maps/api/place/details/json" +
                "?place_id=" + restaurantId +
                "&fields=name,website,formatted_phone_number" +
                "&key=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String jsonData = response.body().string();
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

                    // Récupère l'objet "result"
                    JsonObject resultObject = jsonObject.getAsJsonObject("result");

                    // Récupère le site web du restaurant, si disponible
                    if (resultObject.has("website")) {
                        String website = resultObject.get("website").getAsString();
                        websiteCallback.accept(Optional.ofNullable(website));
                    } else {
                        websiteCallback.accept(Optional.empty());
                    }

                    // Récupère le numéro de téléphone, si disponible
                    if (resultObject.has("formatted_phone_number")) {
                        String phoneNumber = resultObject.get("formatted_phone_number").getAsString();
                        phoneCallback.accept(Optional.ofNullable(phoneNumber));
                    } else {
                        phoneCallback.accept(Optional.empty());
                    }

                } else {
                    Log.e("aaa", "Request failed: " + response);
                }
            } catch (IOException e) {
                Log.e("aaa", "Request error", e);
            }
        }).start();
    }
    private String formatTime(String time) {
        if (time.length() == 4) {
            return time.substring(0, 2) + ":" + time.substring(2);
        }
        return time;
    }
}
