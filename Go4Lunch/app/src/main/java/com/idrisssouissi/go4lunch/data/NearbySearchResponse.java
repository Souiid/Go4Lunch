package com.idrisssouissi.go4lunch.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NearbySearchResponse {
    @SerializedName("results")
    public List<RestaurantResult> results;

    public static class RestaurantResult {
        @SerializedName("place_id")
        public String placeId;

        @SerializedName("name")
        public String name;

        @SerializedName("vicinity")
        public String address;

        @SerializedName("geometry")
        public Geometry geometry;

        @SerializedName("photos")
        public List<Photo> photos;

        public static class Geometry {
            @SerializedName("location")
            public Location location;

            public static class Location {
                @SerializedName("lat")
                public double lat;

                @SerializedName("lng")
                public double lng;
            }
        }

        public static class Photo {
            @SerializedName("photo_reference")
            public String photoReference;
        }
    }
}
