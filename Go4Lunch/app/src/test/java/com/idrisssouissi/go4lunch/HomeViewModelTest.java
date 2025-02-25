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
        // GIVEN
        Observer<Boolean> observer = mock(Observer.class);
        homeViewModel.getUserConnectionStatus().observeForever(observer);

        // WHEN
        homeViewModel.isUserConnected.postValue(true);

        // THEN
        verify(observer, atLeastOnce()).onChanged(true);
        assertEquals(true, homeViewModel.getUserConnectionStatus().getValue());

        // WHEN
        homeViewModel.isUserConnected.postValue(false);

        // THEN
        verify(observer, atLeastOnce()).onChanged(false);
        assertEquals(false, homeViewModel.getUserConnectionStatus().getValue());

        // Nettoyage
        homeViewModel.getUserConnectionStatus().removeObserver(observer);
    }

    @Test
    public void testCheckUserConnection() {
        // GIVEN
        when(firebaseApiService.isUserConnected()).thenReturn(true);
        Observer<Boolean> observer = mock(Observer.class);
        homeViewModel.getUserConnectionStatus().observeForever(observer);

        // WHEN
        homeViewModel.checkUserConnection();

        // THEN
        verify(observer).onChanged(true);
        assertEquals(true, homeViewModel.getUserConnectionStatus().getValue());

        // Nettoyage
        homeViewModel.getUserConnectionStatus().removeObserver(observer);
    }

    @Test
    public void testSignOut() {
        // GIVEN
        Observer<Boolean> observer = mock(Observer.class);
        homeViewModel.getUserConnectionStatus().observeForever(observer);

        // WHEN
        homeViewModel.signOut();

        // THEN
        verify(firebaseApiService).signOut(); // Vérifie que signOut() est bien appelé sur Firebase
        verify(observer, atLeastOnce()).onChanged(false);
        assertEquals(false, homeViewModel.getUserConnectionStatus().getValue());

        // Nettoyage
        homeViewModel.getUserConnectionStatus().removeObserver(observer);
    }

    @Test
    public void testGetRestaurants() {
        // GIVEN
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

        // WHEN
        homeViewModel.getRestaurants();

        // THEN
        verify(observer, atLeastOnce()).onChanged(mockRestaurants);
        assertEquals(mockRestaurants, homeViewModel.getRestaurants().getValue());

        // Nettoyage
        homeViewModel.getRestaurants().removeObserver(observer);
    }


    @Test
    public void testGetLastLocation() {
        // GIVEN
        MutableLiveData<LatLng> mockLocationLiveData = new MutableLiveData<>();
        LatLng mockLocation = new LatLng(48.8566, 2.3522); // Paris
        mockLocationLiveData.setValue(mockLocation);

        when(locationRepository.getLastLocation()).thenReturn(mockLocationLiveData);

        // WHEN
        LiveData<LatLng> result = homeViewModel.getLastLocation();

        // THEN
        assertEquals(mockLocation, result.getValue());
    }

    @Test
    public void testRefreshUsers() {
        // WHEN
        homeViewModel.refreshUsers();

        // THEN
        verify(userRepository, atLeastOnce()).getAllUsers();
    }

    @Test
    public void testSetLastLocation() {
        // GIVEN
        Double latitude = 48.8566;
        Double longitude = 2.3522;
        LatLng expectedLocation = new LatLng(latitude, longitude);

        // WHEN
        homeViewModel.setLastLocation(latitude, longitude);

        // THEN
        verify(locationRepository, times(1)).setLastLocation(expectedLocation);
    }

    @Test
    public void testSortRestaurantsByNote() {
        // GIVEN - Une liste de restaurants non triée
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

        // WHEN - Trier par note décroissante
        homeViewModel.sortRestaurantsByNote(true);
        List<Restaurant> sortedRestaurantsDesc = homeViewModel.restaurantsLiveData.getValue();

        // THEN - Vérifier l'ordre décroissant (5 -> 4 -> 3)
        assertEquals("2", sortedRestaurantsDesc.get(0).getId()); // Note 5
        assertEquals("3", sortedRestaurantsDesc.get(1).getId()); // Note 4
        assertEquals("1", sortedRestaurantsDesc.get(2).getId()); // Note 3

        // WHEN - Trier par note croissante
        homeViewModel.sortRestaurantsByNote(false);
        List<Restaurant> sortedRestaurantsAsc = homeViewModel.restaurantsLiveData.getValue();

        // THEN - Vérifier l'ordre croissant (3 -> 4 -> 5)
        assertEquals("1", sortedRestaurantsAsc.get(0).getId()); // Note 3
        assertEquals("3", sortedRestaurantsAsc.get(1).getId()); // Note 4
        assertEquals("2", sortedRestaurantsAsc.get(2).getId()); // Note 5
    }


    @Test
    public void testSortRestaurantsByDistance() {
        // GIVEN - Une liste de restaurants avec différentes distances
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

        // Ajouter les distances manuellement
        mockRestaurants.get(0).setDistance(1500f); // Restaurant A - 1500m
        mockRestaurants.get(1).setDistance(500f);  // Restaurant B - 500m
        mockRestaurants.get(2).setDistance(1000f); // Restaurant C - 1000m

        homeViewModel.restaurantsLiveData.setValue(new ArrayList<>(mockRestaurants));

        // WHEN - Trier par distance croissante
        homeViewModel.sortRestaurantsByDistance();
        List<Restaurant> sortedRestaurants = homeViewModel.restaurantsLiveData.getValue();

        // Debug : Afficher l'ordre réel des IDs après tri
        System.out.println("Order after sorting by distance:");
        for (Restaurant restaurant : sortedRestaurants) {
            System.out.println(restaurant.getId() + " - " + restaurant.getDistance().get() + "m");
        }

        // THEN - Vérifier l'ordre croissant (500m -> 1000m -> 1500m)
        assertEquals("2", sortedRestaurants.get(0).getId()); // 500m
        assertEquals("3", sortedRestaurants.get(1).getId()); // 1000m
        assertEquals("1", sortedRestaurants.get(2).getId()); // 1500m
    }

    @Test
    public void testGetDistantRestaurantName() throws IOException {
        // GIVEN - Simuler une réponse de restaurantRepository.getRestaurantContact()
        String restaurantId = "123";
        Triple<String, String, String> mockRestaurantContact = new Triple<>("Nom du Restaurant", "0123456789", "https://restaurant.com");

        when(restaurantRepository.getRestaurantContact(restaurantId)).thenReturn(mockRestaurantContact);

        // WHEN - Appeler la méthode
        Triple<String, String, String> result = homeViewModel.getDistantRestaurantName(restaurantId);

        // THEN - Vérifier que la méthode retourne bien la réponse simulée
        assertEquals("Nom du Restaurant", result.getFirst());
        assertEquals("0123456789", result.getSecond());
        assertEquals("https://restaurant.com", result.getThird());
    }

    @Test
    public void testSortRestaurantsByName() {
        // GIVEN - Une liste de restaurants avec des noms non triés
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

        // WHEN - Trier par nom croissant (A -> Z)
        homeViewModel.sortRestaurantsByName(true);
        List<Restaurant> sortedRestaurantsAsc = homeViewModel.restaurantsLiveData.getValue();

        // THEN - Vérifier l'ordre croissant (Alpha -> Mango -> Zebra)
        assertEquals("2", sortedRestaurantsAsc.get(0).getId()); // Alpha
        assertEquals("3", sortedRestaurantsAsc.get(1).getId()); // Mango
        assertEquals("1", sortedRestaurantsAsc.get(2).getId()); // Zebra

        // WHEN - Trier par nom décroissant (Z -> A)
        homeViewModel.sortRestaurantsByName(false);
        List<Restaurant> sortedRestaurantsDesc = homeViewModel.restaurantsLiveData.getValue();

        // THEN - Vérifier l'ordre décroissant (Zebra -> Mango -> Alpha)
        assertEquals("1", sortedRestaurantsDesc.get(0).getId()); // Zebra
        assertEquals("3", sortedRestaurantsDesc.get(1).getId()); // Mango
        assertEquals("2", sortedRestaurantsDesc.get(2).getId()); // Alpha
    }

    @Test
    public void testFilterRestaurantsByName() {
        // GIVEN - Une liste de restaurants avec des noms variés
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

        // WHEN - Filtrer avec "Mango"
        homeViewModel.filterRestaurantsByName("Mango");
        List<Restaurant> filteredRestaurants = homeViewModel.restaurantsLiveData.getValue();

        // THEN - Vérifier que seuls les restaurants contenant "Mango" sont retournés
        assertEquals(2, filteredRestaurants.size());
        assertEquals("2", filteredRestaurants.get(0).getId()); // Mango Delight
        assertEquals("3", filteredRestaurants.get(1).getId()); // Mango Bistro
    }


    @Test
    public void testInitRestaurants() {
        // GIVEN - Une liste de restaurants simulée
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

        // WHEN - Appeler initRestaurants()
        homeViewModel.initRestaurants();

        // THEN - Vérifier que restaurantsLiveData a bien été mis à jour
        verify(observer, atLeastOnce()).onChanged(mockRestaurants);
        assertEquals(mockRestaurants, homeViewModel.getRestaurants().getValue());

        // Nettoyage
        homeViewModel.getRestaurants().removeObserver(observer);
    }

    @Test
    public void testFormatDistance() {
        // GIVEN - Différentes distances
        Float distance1 = 500f;    // Doit afficher "500 m"
        Float distance2 = 1500f;   // Doit afficher "1.5 km" ou "1,5 km"
        Float distance3 = 999.9f;  // Doit afficher "1000 m" (arrondi)
        Float distance4 = 2000f;   // Doit afficher "2.0 km" ou "2,0 km"

        // Récupérer le séparateur décimal du système
        char decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
        String expectedKilometerFormat1 = "1" + decimalSeparator + "5 km";
        String expectedKilometerFormat2 = "2" + decimalSeparator + "0 km";

        // WHEN - Appel de la méthode
        String result1 = homeViewModel.formatDistance(distance1);
        String result2 = homeViewModel.formatDistance(distance2);
        String result3 = homeViewModel.formatDistance(distance3);
        String result4 = homeViewModel.formatDistance(distance4);

        // THEN - Vérifier que les distances sont correctement formatées
        assertEquals("500 m", result1);
        assertEquals(expectedKilometerFormat1, result2);
        assertEquals("1000 m", result3);
        assertEquals(expectedKilometerFormat2, result4);
    }

    @Test
    public void testGetCurrentUser() {
        // GIVEN - Liste d'utilisateurs mockée
        User mockUser1 = mock(User.class);
        when(mockUser1.getId()).thenReturn("user_1");

        User mockUser2 = mock(User.class);
        when(mockUser2.getId()).thenReturn("user_2"); // Utilisateur courant

        List<User> mockUserList = Arrays.asList(mockUser1, mockUser2);

        when(userRepository.getUsersLiveData()).thenReturn(new MutableLiveData<>(mockUserList));
        when(userRepository.getCurrentUID()).thenReturn("user_2"); // Simuler l'ID actuel

        // WHEN - Récupération de l'utilisateur courant
        User currentUser = homeViewModel.getCurrentUser();

        // THEN - Vérifier que l'utilisateur retourné est bien "user_2"
        assertNotNull(currentUser);
        assertEquals("user_2", currentUser.getId());
    }

    @Test
    public void testGetCurrentUser_NoMatch() {
        // GIVEN - Liste d'utilisateurs où aucun ID ne correspond
        User mockUser1 = mock(User.class);
        when(mockUser1.getId()).thenReturn("user_1");

        User mockUser2 = mock(User.class);
        when(mockUser2.getId()).thenReturn("user_3"); // Aucun utilisateur avec "user_2"

        List<User> mockUserList = Arrays.asList(mockUser1, mockUser2);

        when(userRepository.getUsersLiveData()).thenReturn(new MutableLiveData<>(mockUserList));
        when(userRepository.getCurrentUID()).thenReturn("user_2"); // Simuler un ID inexistant

        // WHEN - Récupération de l'utilisateur courant
        User currentUser = homeViewModel.getCurrentUser();

        // THEN - Vérifier que la méthode retourne null
        assertNull(currentUser);
    }





}
