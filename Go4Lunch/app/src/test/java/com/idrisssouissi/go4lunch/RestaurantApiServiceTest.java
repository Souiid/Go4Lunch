package com.idrisssouissi.go4lunch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import com.idrisssouissi.go4lunch.data.NearbySearchResponse;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.RestaurantApiService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class RestaurantApiServiceTest {

    private FakeRestaurantApi fakeRestaurantApi;
    private RestaurantApiService restaurantApiService;

    @Before
    public void setUp() {
        fakeRestaurantApi = new FakeRestaurantApi();
        restaurantApiService = new RestaurantApiService(fakeRestaurantApi);
    }

    @Test
    public void testFetchNearbyRestaurants_success() throws IOException {
        NearbySearchResponse mockResponse = new NearbySearchResponse();
        mockResponse.results = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            NearbySearchResponse.RestaurantResult mockResult = new NearbySearchResponse.RestaurantResult();
            mockResult.placeId = String.valueOf(i);
            mockResult.name = "Restaurant " + i;
            mockResult.address = "Adresse " + i;
            mockResult.geometry = new NearbySearchResponse.RestaurantResult.Geometry();
            mockResult.geometry.location = new NearbySearchResponse.RestaurantResult.Geometry.Location();
            mockResult.geometry.location.lat = 48.8566;
            mockResult.geometry.location.lng = 2.3522;
            mockResponse.results.add(mockResult);
        }

        fakeRestaurantApi.setMockResponse(mockResponse);

        List<Restaurant> restaurants = restaurantApiService.fetchNearbyRestaurants(48.8566, 2.3522);

        assertNotNull(restaurants);
        assertEquals(5, restaurants.size());
    }

    @Test
    public void testFetchNearbyRestaurants_failure() {
        fakeRestaurantApi.setShouldThrowError(true);

        IOException thrownException = null;
        try {
            restaurantApiService.fetchNearbyRestaurants(48.8566, 2.3522);
        } catch (IOException e) {
            thrownException = e;
        }

        assertNotNull("L'exception IOException aurait dû être levée", thrownException);
        assertEquals("Erreur réseau simulée", thrownException.getMessage());
    }
}
