package com.idrisssouissi.go4lunch.data;

import com.google.gson.annotations.SerializedName;

public class RestaurantDetailsResponse {
    @SerializedName("result")
    public RestaurantDetail result;

    public static class RestaurantDetail {
        @SerializedName("name")
        public String name;

        @SerializedName("website")
        public String website;

        @SerializedName("formatted_phone_number")
        public String phoneNumber;
    }
}
