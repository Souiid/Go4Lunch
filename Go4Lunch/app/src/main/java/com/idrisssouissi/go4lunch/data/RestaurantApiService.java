package com.idrisssouissi.go4lunch.data;

import com.idrisssouissi.go4lunch.BuildConfig;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kotlin.Triple;
import retrofit2.Call;
import retrofit2.Response;

public class RestaurantApiService {

    private static final String API_KEY = BuildConfig.GOOGLE_API_KEY;
    private final RestaurantApi restaurantApi;

    public RestaurantApiService() {
        this.restaurantApi = RetrofitClient.getInstance().create(RestaurantApi.class);
    }

    public List<Restaurant> fetchNearbyRestaurants(double latitude, double longitude) throws IOException {
        String location = latitude + "," + longitude;

        Call<NearbySearchResponse> call = restaurantApi.fetchNearbyRestaurants(location, 500, "restaurant", API_KEY);
        Response<NearbySearchResponse> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            List<Restaurant> restaurants = mapResultsToRestaurants(response.body().results);

            for (Restaurant restaurant : restaurants) {
                LocalTime[] openingHours = fetchOpeningHoursForRestaurant(restaurant.getId());
                restaurant.setOpenHours(openingHours);
            }

            return restaurants;
        } else {
            throw new IOException("Request failed with code: " + response.code());
        }
    }

    public LocalTime[] fetchOpeningHoursForRestaurant(String placeId) throws IOException {
        Call<RestaurantDetailsResponse> call = restaurantApi.getRestaurantDetails(
                placeId,
                "opening_hours",
                API_KEY
        );

        Response<RestaurantDetailsResponse> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            RestaurantDetailsResponse.RestaurantDetail result = response.body().result;

            if (result.openingHours != null && result.openingHours.weekdayText != null) {
                String hourlyText = String.valueOf(getDayFromWeekdayText(result.openingHours.weekdayText));
                String hoursFromDay = getHoursFromTheDay(hourlyText);
                return extractHours(hoursFromDay);
            }
        }

        return new LocalTime[2];
    }

    private LocalTime parseToLocalTime(String timeStr) {
        DateTimeFormatter formatterWithAMPM = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        DateTimeFormatter formatter24h = DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH);

        if (timeStr.toUpperCase().contains("AM") || timeStr.toUpperCase().contains("PM")) {
            String cleanedTimeStr = timeStr.replace("\u202F", " ").toUpperCase();
            return LocalTime.parse(cleanedTimeStr, formatterWithAMPM);
        } else {
            String cleanedTimeStr = timeStr.replace("\u202F", " ");
            return LocalTime.parse(cleanedTimeStr, formatter24h);
        }
    }

    private String getHoursFromTheDay(String hourlyText) {
        int colonIndex = hourlyText.indexOf(':');
        String hourlyTextModify = hourlyText;
        if (colonIndex != -1) {
            hourlyTextModify = hourlyText.substring(colonIndex + 1).trim();

            int closingBracketIndex = hourlyTextModify.indexOf(']');
            if (closingBracketIndex != -1) {
                hourlyTextModify = hourlyTextModify.substring(0, closingBracketIndex).trim();
            }

            int commaIndex = hourlyTextModify.indexOf(',');
            if (commaIndex != -1) {
                hourlyTextModify = hourlyTextModify.substring(0, commaIndex).trim();
            }
        }
        return hourlyTextModify;
    }

    private LocalTime[] extractHours(String input) {
        if (input.isEmpty()) {
            return new LocalTime[]{null, null};
        }

        if (input.equals("Closed")) {
            return new LocalTime[]{LocalTime.of(0, 0), LocalTime.of(0, 0)};
        }
        String regex = "(\\d{1,2}:\\d{2}(?:\\s*(?:AM|PM))?)\\s*–\\s*(\\d{1,2}:\\d{2}(?:\\s*(?:AM|PM))?)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String heure1 = Objects.requireNonNull(matcher.group(1)).trim();
            String heure2 = Objects.requireNonNull(matcher.group(2)).trim();

            return new LocalTime[]{parseToLocalTime(heure1), parseToLocalTime(heure2)};
        }

        return new LocalTime[]{null, null};
    }

    private Optional<String> getDayFromWeekdayText(List<String> weekdayText) {
        LocalDate today = LocalDate.now();
        int intDayOfWeek = today.getDayOfWeek().getValue();
        try {
            return Optional.ofNullable(weekdayText.get(intDayOfWeek - 1));
        }catch(Exception e) {
            return Optional.empty();
        }
    }

    private List<Restaurant> mapResultsToRestaurants(List<NearbySearchResponse.RestaurantResult> results) {
        List<Restaurant> restaurants = new ArrayList<>();
        for (NearbySearchResponse.RestaurantResult result : results) {
            String photoUrl = null;

            if (result.photos != null && !result.photos.isEmpty()) {
                photoUrl = getPhotoUrl(result.photos.get(0).photoReference);
            }

            LocalTime[] openHours = new LocalTime[2];


            Restaurant restaurant = new Restaurant(
                    result.placeId,
                    result.name,
                    result.address,
                    result.geometry.location.lat,
                    result.geometry.location.lng,
                    null,
                    photoUrl,
                    openHours,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
            restaurants.add(restaurant);
        }
        return restaurants;
    }

    public Triple<String, String, String> getRestaurantDetailsFromId(String restaurantId, Boolean isForNotification) throws IOException {
        Call<RestaurantDetailsResponse> call = restaurantApi.getRestaurantDetails(
                restaurantId,
                "name,website,formatted_phone_number,opening_hours,formatted_address",
                API_KEY
        );

        Response<RestaurantDetailsResponse> response = call.execute();


        if (response.isSuccessful() && response.body() != null) {
            RestaurantDetailsResponse.RestaurantDetail result = response.body().result;
            if (isForNotification) {
                return new Triple<>(result.name, result.address, result.website);
            }else {
                return new Triple<>(result.name, result.phoneNumber, result.website);

            }
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