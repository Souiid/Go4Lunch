package com.idrisssouissi.go4lunch;

import com.idrisssouissi.go4lunch.data.NearbySearchResponse;
import com.idrisssouissi.go4lunch.data.RestaurantApi;
import com.idrisssouissi.go4lunch.data.RestaurantDetailsResponse;

import java.io.IOException;

import okio.Timeout;
import retrofit2.Call;
import retrofit2.Response;

public class FakeRestaurantApi implements RestaurantApi {
    private NearbySearchResponse mockResponse;
    private RestaurantDetailsResponse mockDetailsResponse;
    private boolean shouldThrowError = false;

    public void setMockResponse(NearbySearchResponse response) {
        this.mockResponse = response;
    }

    public void setMockDetailsResponse(RestaurantDetailsResponse response) {
        this.mockDetailsResponse = response;
    }

    public void setShouldThrowError(boolean shouldThrowError) {
        this.shouldThrowError = shouldThrowError;
    }

    @Override
    public Call<NearbySearchResponse> fetchNearbyRestaurants(String location, int radius, String type, String apiKey) {
        return new Call<NearbySearchResponse>() {
            @Override
            public Response<NearbySearchResponse> execute() throws IOException {
                if (shouldThrowError) {
                    throw new IOException("Erreur réseau simulée");
                }
                return Response.success(mockResponse);
            }

            @Override public void enqueue(retrofit2.Callback<NearbySearchResponse> callback) {}
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @Override public Call<NearbySearchResponse> clone() { return this; }
            @Override public okhttp3.Request request() { return null; }

            @Override
            public Timeout timeout() {
                return null;
            }
        };
    }

    @Override
    public Call<RestaurantDetailsResponse> getRestaurantDetails(String placeId, String fields, String apiKey) {
        return new Call<RestaurantDetailsResponse>() {
            @Override
            public Response<RestaurantDetailsResponse> execute() throws IOException {
                if (shouldThrowError) {
                    throw new IOException("Erreur réseau simulée");
                }
                return Response.success(mockDetailsResponse);
            }

            @Override public void enqueue(retrofit2.Callback<RestaurantDetailsResponse> callback) {}
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @Override public Call<RestaurantDetailsResponse> clone() { return this; }
            @Override public okhttp3.Request request() { return null; }

            @Override
            public Timeout timeout() {
                return null;
            }
        };
    }
}
