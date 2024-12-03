package com.idrisssouissi.go4lunch.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kotlin.Triple;
import retrofit2.Call;
import retrofit2.Response;

public class RestaurantApiService {

    private static final String API_KEY = "AIzaSyBig97MXmqFVmydv38OkE8d0SXxeCaTbtU";
    private final RestaurantApi restaurantApi;

    public RestaurantApiService() {
        this.restaurantApi = RetrofitClient.getInstance().create(RestaurantApi.class);
    }

    public List<Restaurant> fetchNearbyRestaurants(double latitude, double longitude) throws IOException {
        String location = latitude + "," + longitude;
        Call<NearbySearchResponse> call = restaurantApi.fetchNearbyRestaurants(location, 500, "restaurant", API_KEY);

        // Appel synchrone
        Response<NearbySearchResponse> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            // Mappe les RestaurantResult en Restaurant
            return mapResultsToRestaurants(response.body().results);
        } else {
            throw new IOException("Request failed with code: " + response.code());
        }
    }


    private List<Restaurant> mapResultsToRestaurants(List<NearbySearchResponse.RestaurantResult> results) {
        List<Restaurant> restaurants = new ArrayList<>();
        for (NearbySearchResponse.RestaurantResult result : results) {
            String photoUrl = null;

            // Si une photo est disponible, construire l'URL
            if (result.photos != null && !result.photos.isEmpty()) {
                photoUrl = getPhotoUrl(result.photos.get(0).photoReference);
            }

            String openHours = "Unknown";
            if (result.openingHours != null && result.openingHours.openNow != null) {
                openHours = result.openingHours.openNow ? "Open now" : "Closed now";
            }

            Restaurant restaurant = new Restaurant(
                    result.placeId,
                    result.name,
                    result.address,
                    result.geometry.location.lat,
                    result.geometry.location.lng,
                    null, // Type (non présent dans l'API)
                    photoUrl, // URL de la photo
                    openHours, // Horaires d'ouverture (peut être ajusté)
                    Optional.empty(), // Téléphone
                    Optional.empty(), // Site Web
                    Optional.empty(), // Note
                    Optional.empty()  // Nombre d'utilisateurs
            );
            restaurants.add(restaurant);
        }
        return restaurants;
    }



    public Triple<String, String, String> getRestaurantDetailsFromId(String restaurantId) throws IOException {
        Call<RestaurantDetailsResponse> call = restaurantApi.getRestaurantDetails(
                restaurantId,
                "name,website,formatted_phone_number,opening_hours",
                API_KEY
        );

        // Appel synchrone
        Response<RestaurantDetailsResponse> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            RestaurantDetailsResponse.RestaurantDetail result = response.body().result;

            // Mapper les détails dans un objet Restaurant
            return new Triple<>(result.name, result.phoneNumber, result.website);
        } else {
            throw new IOException("Request failed with code: " + response.code());
        }
    }

    private String getPhotoUrl(String photoReference) {
        if (photoReference == null || photoReference.isEmpty()) {
            return null;
        }
        return "https://maps.googleapis.com/maps/api/place/photo" +
                "?maxwidth=400" +
                "&photoreference=" + photoReference +
                "&key=" + API_KEY;
    }
}
