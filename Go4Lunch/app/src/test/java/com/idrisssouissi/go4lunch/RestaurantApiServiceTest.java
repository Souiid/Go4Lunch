package com.idrisssouissi.go4lunch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.idrisssouissi.go4lunch.data.NearbySearchResponse;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.RestaurantApi;
import com.idrisssouissi.go4lunch.data.RestaurantApiService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class RestaurantApiServiceTest {

    @Mock
    private RestaurantApi restaurantApi;

    @Mock
    private Call<NearbySearchResponse> mockCall;

    @InjectMocks
    private RestaurantApiService restaurantApiService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFetchNearbyRestaurants_success() throws IOException {
        // Mock la réponse de l'API avec 20 restaurants
        NearbySearchResponse mockResponse = new NearbySearchResponse();
        mockResponse.results = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
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

        // Mock de l'objet Call
        Call<NearbySearchResponse> mockCall = mock(Call.class);

        // Mock l'API pour retourner la réponse mockée
        when(restaurantApi.fetchNearbyRestaurants(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(mockResponse));

        // Exécuter la méthode
        List<Restaurant> restaurants = restaurantApiService.fetchNearbyRestaurants(48.8566, 2.3522);

        // Vérifications
        assertNotNull(restaurants);
        assertEquals(20, restaurants.size());  // Vérifie bien que 20 restaurants sont récupérés
    }

    @Test(expected = IOException.class)
    public void testFetchNearbyRestaurants_failure() throws IOException {
        // Mock de l'objet Call
        Call<NearbySearchResponse> mockCall = mock(Call.class);

        // ✅ Mock l'API pour renvoyer l'appel mocké
        when(restaurantApi.fetchNearbyRestaurants(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(mockCall);

        // ✅ Forcer l'erreur réseau en lançant une IOException
        when(mockCall.execute()).thenThrow(new IOException("Erreur réseau simulée"));

        // Exécuter la méthode → doit lever une IOException
        restaurantApiService.fetchNearbyRestaurants(48.8566, 2.3522);
    }
}
