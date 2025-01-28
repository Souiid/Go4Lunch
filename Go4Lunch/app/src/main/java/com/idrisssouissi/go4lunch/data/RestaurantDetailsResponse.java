package com.idrisssouissi.go4lunch.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RestaurantDetailsResponse {
    @SerializedName("result")
    public RestaurantDetail result;

    public static class RestaurantDetail {
        @SerializedName("name")
        public String name;

        @SerializedName("website")
        public String website;

        @SerializedName("formatted_address")
        public String address;

        @SerializedName("formatted_phone_number")
        public String phoneNumber;

        @SerializedName("opening_hours")
        public OpeningHours openingHours;

        public static class OpeningHours {
            @SerializedName("weekday_text")
            public List<String> weekdayText;
        }
    }
}
