package com.idrisssouissi.go4lunch.data;

import android.util.Log;

import com.idrisssouissi.go4lunch.BuildConfig;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

        // Appel API Nearby Search pour récupérer les restaurants
        Call<NearbySearchResponse> call = restaurantApi.fetchNearbyRestaurants(location, 500, "restaurant", API_KEY);
        Response<NearbySearchResponse> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            // Mapper les résultats de Nearby Search en objets Restaurant
            List<Restaurant> restaurants = mapResultsToRestaurants(response.body().results);

            // Pour chaque restaurant, récupérer les horaires via Place Details
            for (Restaurant restaurant : restaurants) {
                LocalTime[] openingHours = fetchOpeningHoursForRestaurant(restaurant.getId());
                restaurant.setOpenHours(openingHours); // Mettre à jour les horaires
            }

            return restaurants;
        } else {
            throw new IOException("Request failed with code: " + response.code());
        }
    }

    public LocalTime[] fetchOpeningHoursForRestaurant(String placeId) throws IOException {
        // Effectue un appel à l'API Place Details
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        LocalTime currentTime = LocalTime.now();
        Call<RestaurantDetailsResponse> call = restaurantApi.getRestaurantDetails(
                placeId,
                "opening_hours", // Spécifie qu'on veut récupérer uniquement les horaires d'ouverture
                API_KEY
        );

        Response<RestaurantDetailsResponse> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            RestaurantDetailsResponse.RestaurantDetail result = response.body().result;

            if (result.openingHours != null && result.openingHours.weekdayText != null) {
                Log.d("ggg", "Opening hours" + result.openingHours.weekdayText);
                String hourlyText = String.valueOf(getDayFromWeekdayText(result.openingHours.weekdayText));
                String hoursFromDay = getHoursFromTheDay(hourlyText);
                return extractHours(hoursFromDay);
            }
        }

        return new LocalTime[2];
    }

    private LocalTime parseToLocalTime(String timeStr) {
        // Définir les formats
        DateTimeFormatter formatterWithAMPM = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        DateTimeFormatter formatter24h = DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH);

        // Log avant transformation
        Log.d("www", "Original timeStr: " + timeStr);

        // Vérifier si la chaîne contient AM ou PM
        if (timeStr.toUpperCase().contains("AM") || timeStr.toUpperCase().contains("PM")) {
            String cleanedTimeStr = timeStr.replace("\u202F", " ").toUpperCase();
            LocalTime parsedTime = LocalTime.parse(cleanedTimeStr, formatterWithAMPM);
            return parsedTime;
        } else {
            String cleanedTimeStr = timeStr.replace("\u202F", " ");
            LocalTime parsedTime = LocalTime.parse(cleanedTimeStr, formatter24h);
            return parsedTime;
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
        Log.d("aaa", "HourlyTextModify: " + hourlyTextModify);
        return hourlyTextModify;
    }

    private LocalTime[] extractHours(String input) {
        Log.d("aaa", "Input: " + input);
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
            String heure1 = matcher.group(1).trim();
            String heure2 = matcher.group(2).trim();
            Log.d("aaa", "SimpleHours:" + heure1 + "-" + heure2);

            // Convertir en LocalTime
            return new LocalTime[]{parseToLocalTime(heure1), parseToLocalTime(heure2)};
        }

        return new LocalTime[]{null, null}; // Retourne null si le format n'est pas reconnu
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

            // Si une photo est disponible, construire l'URL
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

    public Triple<String, String, String> getRestaurantDetailsFromId(String restaurantId, Boolean isForNotification) throws IOException {
        Call<RestaurantDetailsResponse> call = restaurantApi.getRestaurantDetails(
                restaurantId,
                "name,website,formatted_phone_number,opening_hours,formatted_address",
                API_KEY
        );

        // Appel synchrone
        Response<RestaurantDetailsResponse> response = call.execute();


        if (response.isSuccessful() && response.body() != null) {
            RestaurantDetailsResponse.RestaurantDetail result = response.body().result;
            Log.d("ppp", "RestaurantDetails: " + result.address);

            // Mapper les détails dans un objet Restaurant`
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