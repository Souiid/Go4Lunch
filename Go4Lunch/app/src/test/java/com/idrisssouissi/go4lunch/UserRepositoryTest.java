package com.idrisssouissi.go4lunch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.User;
import com.idrisssouissi.go4lunch.data.UserRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class UserRepositoryTest {

    @Mock private FirebaseApiService mockFirebaseApiService;
    @Mock private FirebaseAuth mockFirebaseAuth;
    @Mock private FirebaseUser mockFirebaseUser;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private UserRepository userRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        initForTest();
    }

    private void initForTest() {
        try (MockedStatic<FirebaseAuth> mockedFirebaseAuth = Mockito.mockStatic(FirebaseAuth.class)) {
            mockedFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(mockFirebaseAuth);
            when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
            when(mockFirebaseUser.getUid()).thenReturn("mockUID");
            userRepository = new UserRepository(mockFirebaseApiService);
        }
    }

    @Test
    public void getCurrentUID_shouldReturnUserID_whenUserIsAuthenticated() {
        assertEquals("mockUID", userRepository.getCurrentUID());
    }

    @Test
    public void getCurrentUID_shouldReturnNull_whenUserIsNotAuthenticated() {
        try (MockedStatic<FirebaseAuth> mockedFirebaseAuth = Mockito.mockStatic(FirebaseAuth.class)) {
            mockedFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(mockFirebaseAuth);
            when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);

            userRepository = new UserRepository(mockFirebaseApiService);

            assertNull(userRepository.getCurrentUID());
        }
    }

    @Test
    public void getAllUsers_shouldUpdateLiveData() {
        Map<String, Object> selectedRestaurant = new HashMap<>();
        selectedRestaurant.put("id", "resto123");
        selectedRestaurant.put("name", "Le Gourmet");

        List<User> mockUsers = Arrays.asList(
                new User("1", "user1@example.com",
                        "User One", "url1",
                        selectedRestaurant, Arrays.asList("restoA", "restoB")),
                new User("2", "user2@example.com",
                        "User Two", "url2",
                        selectedRestaurant, Arrays.asList("restoC", "restoD"))
        );

        Observer<List<User>> observer = mock(Observer.class);
        userRepository.getUsersLiveData().observeForever(observer);

        doAnswer(invocation -> {
            userRepository.getUsersLiveData().setValue(mockUsers);
            return null;
        }).when(mockFirebaseApiService).getAllUsers(any());

        userRepository.getAllUsers();

        verify(observer).onChanged(mockUsers);

        userRepository.getUsersLiveData().removeObserver(observer);
    }
}