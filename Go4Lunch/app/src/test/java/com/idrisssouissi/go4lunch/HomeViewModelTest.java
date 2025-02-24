package com.idrisssouissi.go4lunch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.idrisssouissi.go4lunch.data.*;
import com.idrisssouissi.go4lunch.ui.HomeViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import kotlin.Triple;

public class HomeViewModelTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private RestaurantRepository mockRestaurantRepository;
    private UserRepository mockUserRepository;
    private FirebaseApiService mockFirebaseApiService;
    private LocationRepository mockLocationRepository;
    private HomeViewModel homeViewModel;

    @Before
    public void setUp() {
        mockRestaurantRepository = Mockito.mock(RestaurantRepository.class);
        mockUserRepository = Mockito.mock(UserRepository.class);
        mockFirebaseApiService = Mockito.mock(FirebaseApiService.class);
        mockLocationRepository = Mockito.mock(LocationRepository.class);

        homeViewModel = new HomeViewModel(
                mockRestaurantRepository, mockUserRepository, mockLocationRepository, mockFirebaseApiService
        );
    }

    @Test
    public void testGetRestaurantsByFetch_updatesLiveData() throws IOException {
        // Arrange
        Double latitude = 48.8566;
        Double longitude = 2.3522;
        List<Restaurant> expectedRestaurants = Arrays.asList(
                new Restaurant("1", "Restaurant A", "Adresse A", latitude, longitude, null, null, new LocalTime[]{}, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
                new Restaurant("2", "Restaurant B", "Adresse B", latitude, longitude, null, null, new LocalTime[]{}, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())
        );

        when(mockRestaurantRepository.getRestaurantsByLocation(latitude, longitude)).thenReturn(expectedRestaurants);

        // Act
        homeViewModel.getRestaurantsByFetch(latitude, longitude);

        // Assert
        assertEquals(expectedRestaurants, homeViewModel.getRestaurants().getValue());
        verify(mockRestaurantRepository, times(1)).getRestaurantsByLocation(latitude, longitude);
    }

    @Test
    public void testSortRestaurantsByNote() {
        // Arrange
        Restaurant restaurant1 = new Restaurant("1", "Restaurant A", "Adresse A", 48.8566, 2.3522, null, null, new LocalTime[]{}, Optional.empty(), Optional.empty(), Optional.of(3), Optional.empty());
        Restaurant restaurant2 = new Restaurant("2", "Restaurant B", "Adresse B", 48.8566, 2.3522, null, null, new LocalTime[]{}, Optional.empty(), Optional.empty(), Optional.of(5), Optional.empty());

        List<Restaurant> restaurants = new ArrayList<>(Arrays.asList(restaurant1, restaurant2));

        MutableLiveData<List<Restaurant>> liveData = new MutableLiveData<>(restaurants);
        when(mockRestaurantRepository.getRestaurants()).thenReturn(restaurants);

        Observer<List<Restaurant>> observer = mock(Observer.class);
        homeViewModel.getRestaurants().observeForever(observer);

        // Act
        homeViewModel.sortRestaurantsByNote(true);

        // Capture les valeurs mises à jour
        ArgumentCaptor<List<Restaurant>> captor = ArgumentCaptor.forClass(List.class);
        verify(observer, atLeastOnce()).onChanged(captor.capture());

        List<Restaurant> sortedRestaurants = captor.getValue();

        // Assert
        assertNotNull(sortedRestaurants);
        assertEquals("Restaurant B", sortedRestaurants.get(0).getName()); // 5 étoiles en premier
        assertEquals("Restaurant A", sortedRestaurants.get(1).getName()); // 3 étoiles après

        // Nettoyage
        homeViewModel.getRestaurants().removeObserver(observer);
    }

    @Test
    public void testCheckUserConnection_updatesLiveData() {
        // Arrange
        when(mockFirebaseApiService.isUserConnected()).thenReturn(true);

        // Act
        homeViewModel.checkUserConnection();

        // Assert
        assertTrue(homeViewModel.getUserConnectionStatus().getValue());
    }

    @Test
    public void testGetDistance_calculatesCorrectly() {
        // Arrange
        LatLng location1 = new LatLng(48.8566, 2.3522); // Paris
        LatLng location2 = new LatLng(48.8584, 2.2945); // Tour Eiffel

        // Act
        Float distance = homeViewModel.getDistance(location1, location2);

        // Assert
        assertNotNull(distance);
        assertTrue(distance > 0);
    }

    @Test
    public void testGetAllSelectedRestaurantID_filtersCorrectly() {
        // Arrange
        Timestamp validDate = new Timestamp(new Date()); // Aujourd'hui
        Timestamp oldDate = new Timestamp(new Date(System.currentTimeMillis() - 86400000)); // Hier

        Map<String, Object> selectedRestaurantToday = Map.of("id", "R1", "date", validDate);
        Map<String, Object> selectedRestaurantYesterday = Map.of("id", "R2", "date", oldDate);

        User user1 = new User("1", "alice@example.com", "Alice", "https://photo.com/alice", selectedRestaurantToday, new ArrayList<>());
        User user2 = new User("2", "bob@example.com", "Bob", "https://photo.com/bob", selectedRestaurantYesterday, new ArrayList<>());

        List<User> users = Arrays.asList(user1, user2);

        Restaurant restaurant1 = new Restaurant("R1", "Restaurant A", "Adresse A", 48.8566, 2.3522, null, null, new LocalTime[]{}, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        Restaurant restaurant2 = new Restaurant("R2", "Restaurant B", "Adresse B", 48.8566, 2.3522, null, null, new LocalTime[]{}, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        List<Restaurant> restaurants = Arrays.asList(restaurant1, restaurant2);

        // Act
        List<String> result = homeViewModel.getAllSelectedRestaurantID(users, restaurants);

        // Assert
        assertEquals(1, result.size());
        assertEquals("R1", result.get(0)); // Seul "R1" est valide pour aujourd’hui
    }

    @Test
    public void testSetLastLocation_updatesLiveData() {
        // Arrange
        LatLng newLocation = new LatLng(40.7128, -74.0060); // New York

        // Act
        homeViewModel.setLastLocation(newLocation.latitude, newLocation.longitude);

        // Assert
        assertEquals(newLocation, homeViewModel.getLastLocation().getValue());
    }

    @Test
    public void testSignOut_updatesLiveData() {
        // Arrange
        doNothing().when(mockFirebaseApiService).signOut();

        // Act
        homeViewModel.signOut();

        // Assert
        assertFalse(homeViewModel.getUserConnectionStatus().getValue());
    }

}