package com.idrisssouissi.go4lunch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.LocationRepository;
import com.idrisssouissi.go4lunch.data.Restaurant;
import com.idrisssouissi.go4lunch.data.RestaurantRepository;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.data.UserRepository;
import com.idrisssouissi.go4lunch.ui.HomeViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import kotlin.Triple;

public class HomeViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private FirebaseApiService firebaseApiService;

    private HomeViewModel homeViewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        MutableLiveData<List<User>> usersLiveDataMock = new MutableLiveData<>(new ArrayList<>());
        when(userRepository.getUsersLiveData()).thenReturn(usersLiveDataMock);

        homeViewModel = new HomeViewModel(restaurantRepository, userRepository, locationRepository, firebaseApiService);
    }

    @Test
    public void testGetRestaurantsByFetch() throws IOException {
        Double latitude = 48.8566;
        Double longitude = 2.3522;

        List<Restaurant> mockRestaurants = Arrays.asList(
                new Restaurant(
                        "1", "Restaurant A", "123 Rue de Paris",
                        latitude, longitude, "Français",
                        "https://example.com/photoA.jpg",
                        new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(22, 0)},
                        Optional.of("0123456789"),
                        Optional.of("https://restaurantA.com"),
                        Optional.of(4),
                        Optional.of(100)
                ),
                new Restaurant(
                        "2", "Restaurant B", "456 Avenue de Lyon",
                        latitude, longitude, "Italien",
                        "https://example.com/photoB.jpg",
                        new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(23, 0)},
                        Optional.empty(),
                        Optional.of("https://restaurantB.com"),
                        Optional.of(5),
                        Optional.of(50)
                )
        );

        when(restaurantRepository.getRestaurantsByLocation(latitude, longitude)).thenReturn(mockRestaurants);

        Observer<List<Restaurant>> observer = mock(Observer.class);
        homeViewModel.restaurantsLiveData.observeForever(observer);

        homeViewModel.getRestaurantsByFetch(latitude, longitude);

        verify(observer).onChanged(mockRestaurants);
        assertEquals(mockRestaurants, homeViewModel.restaurantsLiveData.getValue());

        homeViewModel.restaurantsLiveData.removeObserver(observer);
    }

    @Test
    public void testGetUserConnectionStatus() {
        Observer<Boolean> observer = mock(Observer.class);
        homeViewModel.getUserConnectionStatus().observeForever(observer);

        homeViewModel.isUserConnected.postValue(true);

        verify(observer, atLeastOnce()).onChanged(true);
        assertEquals(true, homeViewModel.getUserConnectionStatus().getValue());

        homeViewModel.isUserConnected.postValue(false);

        verify(observer, atLeastOnce()).onChanged(false);
        assertEquals(false, homeViewModel.getUserConnectionStatus().getValue());

        homeViewModel.getUserConnectionStatus().removeObserver(observer);
    }

    @Test
    public void testCheckUserConnection() {
        when(firebaseApiService.isUserConnected()).thenReturn(true);
        Observer<Boolean> observer = mock(Observer.class);
        homeViewModel.getUserConnectionStatus().observeForever(observer);

        homeViewModel.checkUserConnection();

        verify(observer).onChanged(true);
        assertEquals(true, homeViewModel.getUserConnectionStatus().getValue());

        homeViewModel.getUserConnectionStatus().removeObserver(observer);
    }

    @Test
    public void testSignOut() {
        Observer<Boolean> observer = mock(Observer.class);
        homeViewModel.getUserConnectionStatus().observeForever(observer);

        homeViewModel.signOut();

        verify(firebaseApiService).signOut();
        verify(observer, atLeastOnce()).onChanged(false);
        assertEquals(false, homeViewModel.getUserConnectionStatus().getValue());

        homeViewModel.getUserConnectionStatus().removeObserver(observer);
    }

    @Test
    public void testGetRestaurants() {
        List<Restaurant> mockRestaurants = Arrays.asList(
                new Restaurant(
                        "1", "Restaurant A", "123 Rue de Paris",
                        48.8566, 2.3522, "Français",
                        "https://example.com/photoA.jpg",
                        new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(22, 0)},
                        Optional.of("0123456789"),
                        Optional.of("https://restaurantA.com"),
                        Optional.of(4),
                        Optional.of(100)
                ),
                new Restaurant(
                        "2", "Restaurant B", "456 Avenue de Lyon",
                        48.8566, 2.3522, "Italien",
                        "https://example.com/photoB.jpg",
                        new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(23, 0)},
                        Optional.empty(),
                        Optional.of("https://restaurantB.com"),
                        Optional.of(5),
                        Optional.of(50)
                )
        );

        when(restaurantRepository.getRestaurants()).thenReturn(mockRestaurants);

        Observer<List<Restaurant>> observer = mock(Observer.class);
        homeViewModel.getRestaurants().observeForever(observer);

        homeViewModel.getRestaurants();

        verify(observer, atLeastOnce()).onChanged(mockRestaurants);
        assertEquals(mockRestaurants, homeViewModel.getRestaurants().getValue());

        homeViewModel.getRestaurants().removeObserver(observer);
    }


    @Test
    public void testGetLastLocation() {
        MutableLiveData<LatLng> mockLocationLiveData = new MutableLiveData<>();
        LatLng mockLocation = new LatLng(48.8566, 2.3522); // Paris
        mockLocationLiveData.setValue(mockLocation);

        when(locationRepository.getLastLocation()).thenReturn(mockLocationLiveData);

        LiveData<LatLng> result = homeViewModel.getLastLocation();

        assertEquals(mockLocation, result.getValue());
    }

    @Test
    public void testRefreshUsers() {
        homeViewModel.refreshUsers();

        verify(userRepository, atLeastOnce()).getAllUsers();
    }

    @Test
    public void testSetLastLocation() {
        Double latitude = 48.8566;
        Double longitude = 2.3522;
        LatLng expectedLocation = new LatLng(latitude, longitude);

        homeViewModel.setLastLocation(latitude, longitude);

        verify(locationRepository, times(1)).setLastLocation(expectedLocation);
    }

    @Test
    public void testSortRestaurantsByNote() {
        List<Restaurant> mockRestaurants = Arrays.asList(
                new Restaurant("1", "Restaurant A", "123 Rue de Paris", 48.8566, 2.3522, "Français",
                        "https://example.com/photoA.jpg", new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(22, 0)},
                        Optional.of("0123456789"), Optional.of("https://restaurantA.com"), Optional.of(3), Optional.of(100)),

                new Restaurant("2", "Restaurant B", "456 Avenue de Lyon", 48.8566, 2.3522, "Italien",
                        "https://example.com/photoB.jpg", new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(23, 0)},
                        Optional.empty(), Optional.of("https://restaurantB.com"), Optional.of(5), Optional.of(50)),

                new Restaurant("3", "Restaurant C", "789 Boulevard Haussmann", 48.8566, 2.3522, "Japonais",
                        "https://example.com/photoC.jpg", new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(20, 0)},
                        Optional.of("0987654321"), Optional.of("https://restaurantC.com"), Optional.of(4), Optional.of(75))
        );

        homeViewModel.restaurantsLiveData.setValue(new ArrayList<>(mockRestaurants));

        homeViewModel.sortRestaurantsByNote(true);
        List<Restaurant> sortedRestaurantsDesc = homeViewModel.restaurantsLiveData.getValue();

        assertEquals("2", sortedRestaurantsDesc.get(0).getId()); // Note 5
        assertEquals("3", sortedRestaurantsDesc.get(1).getId()); // Note 4
        assertEquals("1", sortedRestaurantsDesc.get(2).getId()); // Note 3

        homeViewModel.sortRestaurantsByNote(false);
        List<Restaurant> sortedRestaurantsAsc = homeViewModel.restaurantsLiveData.getValue();

        assertEquals("1", sortedRestaurantsAsc.get(0).getId()); // Note 3
        assertEquals("3", sortedRestaurantsAsc.get(1).getId()); // Note 4
        assertEquals("2", sortedRestaurantsAsc.get(2).getId()); // Note 5
    }


    @Test
    public void testSortRestaurantsByDistance() {
        List<Restaurant> mockRestaurants = Arrays.asList(
                new Restaurant("1", "Restaurant A", "123 Rue de Paris", 48.8566, 2.3522, "Français",
                        "https://example.com/photoA.jpg", new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(22, 0)},
                        Optional.of("0123456789"), Optional.of("https://restaurantA.com"), Optional.of(3), Optional.of(100)),

                new Restaurant("2", "Restaurant B", "456 Avenue de Lyon", 48.8566, 2.3522, "Italien",
                        "https://example.com/photoB.jpg", new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(23, 0)},
                        Optional.empty(), Optional.of("https://restaurantB.com"), Optional.of(5), Optional.of(50)),

                new Restaurant("3", "Restaurant C", "789 Boulevard Haussmann", 48.8566, 2.3522, "Japonais",
                        "https://example.com/photoC.jpg", new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(20, 0)},
                        Optional.of("0987654321"), Optional.of("https://restaurantC.com"), Optional.of(4), Optional.of(75))
        );

        mockRestaurants.get(0).setDistance(1500f);
        mockRestaurants.get(1).setDistance(500f);
        mockRestaurants.get(2).setDistance(1000f);

        homeViewModel.restaurantsLiveData.setValue(new ArrayList<>(mockRestaurants));

        homeViewModel.sortRestaurantsByDistance();
        List<Restaurant> sortedRestaurants = homeViewModel.restaurantsLiveData.getValue();

        assertEquals("2", sortedRestaurants.get(0).getId()); // 500m
        assertEquals("3", sortedRestaurants.get(1).getId()); // 1000m
        assertEquals("1", sortedRestaurants.get(2).getId()); // 1500m
    }

    @Test
    public void testGetDistantRestaurantName() throws IOException {
        String restaurantId = "123";
        Triple<String, String, String> mockRestaurantContact = new Triple<>("Nom du Restaurant", "0123456789", "https://restaurant.com");

        when(restaurantRepository.getRestaurantContact(restaurantId)).thenReturn(mockRestaurantContact);

        Triple<String, String, String> result = homeViewModel.getDistantRestaurantName(restaurantId);

        assertEquals("Nom du Restaurant", result.getFirst());
        assertEquals("0123456789", result.getSecond());
        assertEquals("https://restaurant.com", result.getThird());
    }

    @Test
    public void testSortRestaurantsByName() {
        List<Restaurant> mockRestaurants = Arrays.asList(
                new Restaurant("1", "Zebra", "123 Rue de Paris", 48.8566, 2.3522, "Français",
                        "https://example.com/photoA.jpg", new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(22, 0)},
                        Optional.of("0123456789"), Optional.of("https://restaurantA.com"), Optional.of(3), Optional.of(100)),

                new Restaurant("2", "Alpha", "456 Avenue de Lyon", 48.8566, 2.3522, "Italien",
                        "https://example.com/photoB.jpg", new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(23, 0)},
                        Optional.empty(), Optional.of("https://restaurantB.com"), Optional.of(5), Optional.of(50)),

                new Restaurant("3", "Mango", "789 Boulevard Haussmann", 48.8566, 2.3522, "Japonais",
                        "https://example.com/photoC.jpg", new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(20, 0)},
                        Optional.of("0987654321"), Optional.of("https://restaurantC.com"), Optional.of(4), Optional.of(75))
        );

        homeViewModel.restaurantsLiveData.setValue(new ArrayList<>(mockRestaurants));

        homeViewModel.sortRestaurantsByName(true);
        List<Restaurant> sortedRestaurantsAsc = homeViewModel.restaurantsLiveData.getValue();

        assertEquals("2", sortedRestaurantsAsc.get(0).getId()); // Alpha
        assertEquals("3", sortedRestaurantsAsc.get(1).getId()); // Mango
        assertEquals("1", sortedRestaurantsAsc.get(2).getId()); // Zebra

        homeViewModel.sortRestaurantsByName(false);
        List<Restaurant> sortedRestaurantsDesc = homeViewModel.restaurantsLiveData.getValue();

        assertEquals("1", sortedRestaurantsDesc.get(0).getId()); // Zebra
        assertEquals("3", sortedRestaurantsDesc.get(1).getId()); // Mango
        assertEquals("2", sortedRestaurantsDesc.get(2).getId()); // Alpha
    }

    @Test
    public void testFilterRestaurantsByName() {
        List<Restaurant> mockRestaurants = Arrays.asList(
                new Restaurant("1", "Zebra Café", "123 Rue de Paris", 48.8566, 2.3522, "Français",
                        "https://example.com/photoA.jpg", new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(22, 0)},
                        Optional.of("0123456789"), Optional.of("https://restaurantA.com"), Optional.of(3), Optional.of(100)),

                new Restaurant("2", "Mango Delight", "456 Avenue de Lyon", 48.8566, 2.3522, "Italien",
                        "https://example.com/photoB.jpg", new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(23, 0)},
                        Optional.empty(), Optional.of("https://restaurantB.com"), Optional.of(5), Optional.of(50)),

                new Restaurant("3", "Mango Bistro", "789 Boulevard Haussmann", 48.8566, 2.3522, "Japonais",
                        "https://example.com/photoC.jpg", new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(20, 0)},
                        Optional.of("0987654321"), Optional.of("https://restaurantC.com"), Optional.of(4), Optional.of(75))
        );

        when(restaurantRepository.getRestaurants()).thenReturn(mockRestaurants);

        homeViewModel.filterRestaurantsByName("Mango");
        List<Restaurant> filteredRestaurants = homeViewModel.restaurantsLiveData.getValue();

        assertEquals(2, filteredRestaurants.size());
        assertEquals("2", filteredRestaurants.get(0).getId());
        assertEquals("3", filteredRestaurants.get(1).getId());
    }


    @Test
    public void testInitRestaurants() {
        List<Restaurant> mockRestaurants = Arrays.asList(
                new Restaurant("1", "Le Gourmet", "123 Rue de Paris", 48.8566, 2.3522, "Français",
                        "https://example.com/photoA.jpg", new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(22, 0)},
                        Optional.of("0123456789"), Optional.of("https://restaurantA.com"), Optional.of(3), Optional.of(100)),

                new Restaurant("2", "Pizza House", "456 Avenue de Lyon", 48.8566, 2.3522, "Italien",
                        "https://example.com/photoB.jpg", new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(23, 0)},
                        Optional.empty(), Optional.of("https://restaurantB.com"), Optional.of(5), Optional.of(50))
        );

        when(restaurantRepository.getRestaurants()).thenReturn(mockRestaurants);

        Observer<List<Restaurant>> observer = mock(Observer.class);
        homeViewModel.getRestaurants().observeForever(observer);

        homeViewModel.initRestaurants();

        verify(observer, atLeastOnce()).onChanged(mockRestaurants);
        assertEquals(mockRestaurants, homeViewModel.getRestaurants().getValue());

        homeViewModel.getRestaurants().removeObserver(observer);
    }

    @Test
    public void testFormatDistance() {
        Float distance1 = 500f;
        Float distance2 = 1500f;
        Float distance3 = 999.9f;
        Float distance4 = 2000f;

        char decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
        String expectedKilometerFormat1 = "1" + decimalSeparator + "5 km";
        String expectedKilometerFormat2 = "2" + decimalSeparator + "0 km";

        String result1 = homeViewModel.formatDistance(distance1);
        String result2 = homeViewModel.formatDistance(distance2);
        String result3 = homeViewModel.formatDistance(distance3);
        String result4 = homeViewModel.formatDistance(distance4);

        assertEquals("500 m", result1);
        assertEquals(expectedKilometerFormat1, result2);
        assertEquals("1000 m", result3);
        assertEquals(expectedKilometerFormat2, result4);
    }

    @Test
    public void testGetCurrentUser() {
        User mockUser1 = mock(User.class);
        when(mockUser1.getId()).thenReturn("user_1");

        User mockUser2 = mock(User.class);
        when(mockUser2.getId()).thenReturn("user_2");

        List<User> mockUserList = Arrays.asList(mockUser1, mockUser2);

        when(userRepository.getUsersLiveData()).thenReturn(new MutableLiveData<>(mockUserList));
        when(userRepository.getCurrentUID()).thenReturn("user_2");


        User currentUser = homeViewModel.getCurrentUser();

        assertNotNull(currentUser);
        assertEquals("user_2", currentUser.getId());
    }

    @Test
    public void testGetCurrentUser_NoMatch() {
        User mockUser1 = mock(User.class);
        when(mockUser1.getId()).thenReturn("user_1");

        User mockUser2 = mock(User.class);
        when(mockUser2.getId()).thenReturn("user_3");

        List<User> mockUserList = Arrays.asList(mockUser1, mockUser2);

        when(userRepository.getUsersLiveData()).thenReturn(new MutableLiveData<>(mockUserList));
        when(userRepository.getCurrentUID()).thenReturn("user_2");

        User currentUser = homeViewModel.getCurrentUser();

        assertNull(currentUser);
    }
}
