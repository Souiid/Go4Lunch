package com.idrisssouissi.go4lunch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.RestaurantApiService;
import com.idrisssouissi.go4lunch.data.RestaurantRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import kotlin.Triple;

public class RestaurantRepositoryTest {

    private RestaurantApiService mockApiService;
    private RestaurantRepository repository;

    @Before
    public void setUp() {
        mockApiService = Mockito.mock(RestaurantApiService.class);
        repository = new RestaurantRepository(mockApiService);
    }

    @Test
    public void testGetRestaurantsByLocation_returnsRestaurants() throws IOException {
        // Arrange
        Double latitude = 48.8566;
        Double longitude = 2.3522;
        List<Restaurant> expectedRestaurants = Arrays.asList(
                new Restaurant("1", "Restaurant A", "Adresse A", latitude, longitude, null, null, null, null, null, null, null),
                new Restaurant("2", "Restaurant B", "Adresse B", latitude, longitude, null, null, null, null, null, null, null)
        );

        when(mockApiService.fetchNearbyRestaurants(latitude, longitude)).thenReturn(expectedRestaurants);

        // Act
        List<Restaurant> result = repository.getRestaurantsByLocation(latitude, longitude);

        // Assert
        assertEquals(expectedRestaurants, result);
        verify(mockApiService, times(1)).fetchNearbyRestaurants(latitude, longitude);
    }

    @Test
    public void testGetRestaurantById_returnsCorrectRestaurant() throws IOException {
        // Arrange
        Restaurant restaurant1 = new Restaurant("1", "Restaurant A", "Adresse A", 48.8566, 2.3522, null, null, null, null, null, null, null);
        Restaurant restaurant2 = new Restaurant("2", "Restaurant B", "Adresse B", 48.8566, 2.3522, null, null, null, null, null, null, null);
        repository.getRestaurantsByLocation(48.8566, 2.3522); // Simule un appel
        repository.getRestaurants().addAll(Arrays.asList(restaurant1, restaurant2));

        // Act
        Restaurant result = repository.getRestaurantById("2");

        // Assert
        assertEquals(restaurant2, result);
    }

    @Test
    public void testGetRestaurantContact_returnsCorrectDetails() throws IOException {
        // Arrange
        String restaurantId = "1";
        Triple<String, String, String> expectedContact = new Triple<>("Restaurant A", "0123456789", "www.restaurantA.com");

        when(mockApiService.getRestaurantDetailsFromId(restaurantId, false)).thenReturn(expectedContact);

        // Act
        Triple<String, String, String> result = repository.getRestaurantContact(restaurantId);

        // Assert
        assertEquals(expectedContact, result);
        verify(mockApiService, times(1)).getRestaurantDetailsFromId(restaurantId, false);
    }


}
