package com.idrisssouissi.go4lunch.data;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestaurantApi {
    // Endpoint pour récupérer les restaurants à proximité
    @GET("place/nearbysearch/json")
    Call<NearbySearchResponse> fetchNearbyRestaurants(
            @Query("location") String location,
            @Query("radius") int radius,
            @Query("type") String type,
            @Query("key") String apiKey
    );

    // Endpoint pour récupérer les détails d'un restaurant par ID
    @GET("place/details/json")
    Call<RestaurantDetailsResponse> getRestaurantDetails(
            @Query("place_id") String placeId,
            @Query("fields") String fields,
            @Query("key") String apiKey
    );
}

