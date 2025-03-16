package com.idrisssouissi.go4lunch;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.android.gms.tasks.Task;
import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.FirebaseAuthProvider;
import com.idrisssouissi.go4lunch.data.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseApiServiceTest {

    @Mock private FirebaseFirestore mockFirestore;
    @Mock private CollectionReference mockUsersCollection;
    @Mock private DocumentReference mockUserDocument;
    @Mock private Task<Void> mockTask;
    @Mock private Consumer<List<User>> mockCompletion;
    @Mock private FirebaseUser mockUser;
    @Mock private FirebaseAuthProvider mockAuthProvider;

    @Mock private FirebaseAuth mockFirebaseAuth;
    @Mock private Context mockContext;
    @Mock private SharedPreferences mockSharedPreferences;
    @Mock private SharedPreferences.Editor mockEditor;

    private FirebaseApiService firebaseApiService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockFirestore.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.document(anyString())).thenReturn(mockUserDocument);
        when(mockUserDocument.set(any(Map.class), any(SetOptions.class))).thenReturn(mockTask);

        when(mockUser.getUid()).thenReturn("user123");
        when(mockUser.getDisplayName()).thenReturn("Test User");
        Uri mockUri = mock(Uri.class);
        when(mockUri.toString()).thenReturn("https://example.com/user1.jpg");
        when(mockUser.getPhotoUrl()).thenReturn(mockUri);
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);

        when(mockContext.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE))
                .thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(any(String.class), any(String.class))).thenReturn(mockEditor);

        firebaseApiService = new FirebaseApiService(mockFirestore, mockAuthProvider);
    }


    @Test
    public void createUserInFirestore_shouldAddUserToFirestoreAndRunCompletion() {
        ArgumentCaptor<OnSuccessListener<Void>> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        when(mockTask.addOnSuccessListener(successCaptor.capture())).thenReturn(mockTask);

        firebaseApiService.createUserInFirestore(mockUser, () -> mockCompletion.accept(null));

        ArgumentCaptor<Map<String, Object>> userDataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockUserDocument).set(userDataCaptor.capture(), eq(SetOptions.merge()));

        Map<String, Object> capturedData = userDataCaptor.getValue();
        assertEquals("Test User", capturedData.get("name"));
        assertEquals("test@example.com", capturedData.get("email"));
        assertTrue(capturedData.containsKey("selectedRestaurant"));
        assertTrue(capturedData.containsKey("restaurantLikeIDs"));

        successCaptor.getValue().onSuccess(null);
        verify(mockCompletion).accept(null);
    }

    @Test
    public void signOut_shouldCallAuthProviderSignOut() {
        firebaseApiService.signOut();
        verify(mockAuthProvider).signOut();
    }

    @Test
    public void isUserConnected_shouldReturnTrue_whenUserIsConnected() {
        when(mockAuthProvider.getCurrentUser()).thenReturn(mockUser);

        Boolean result = firebaseApiService.isUserConnected();

        assertTrue(result);
    }

    @Test
    public void isUserConnected_shouldReturnFalse_whenUserIsNotConnected() {
        when(mockAuthProvider.getCurrentUser()).thenReturn(null);

        Boolean result = firebaseApiService.isUserConnected();

        assertFalse(result);
    }

    @Test
    public void getAllUsers_shouldRetrieveUsersFromFirestoreAndPassToCompletion() {
        DocumentSnapshot mockDocument1 = mock(DocumentSnapshot.class);
        DocumentSnapshot mockDocument2 = mock(DocumentSnapshot.class);
        Task<QuerySnapshot> mockTask = mock(Task.class);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);

        List<DocumentSnapshot> mockDocuments = List.of(mockDocument1, mockDocument2);

        when(mockFirestore.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.get()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshot.iterator()).thenAnswer(invocation -> mockDocuments.iterator());

        doAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onComplete(mockTask);
            return mockTask;
        }).when(mockTask).addOnCompleteListener(any());

        when(mockDocument1.getId()).thenReturn("user1");
        when(mockDocument1.getString("email")).thenReturn("user1@example.com");
        when(mockDocument1.getString("name")).thenReturn("User One");
        when(mockDocument1.getString("photoUrl")).thenReturn("https://example.com/user1.jpg");
        when(mockDocument1.get("selectedRestaurant")).thenReturn(new HashMap<String, Object>());
        when(mockDocument1.get("restaurantLikeIDs")).thenReturn(List.of("restaurant1", "restaurant2"));

        when(mockDocument2.getId()).thenReturn("user2");
        when(mockDocument2.getString("email")).thenReturn("user2@example.com");
        when(mockDocument2.getString("name")).thenReturn("User Two");
        when(mockDocument2.getString("photoUrl")).thenReturn("https://example.com/user2.jpg");
        when(mockDocument2.get("selectedRestaurant")).thenReturn(new HashMap<String, Object>());
        when(mockDocument2.get("restaurantLikeIDs")).thenReturn(List.of("restaurant3"));

        ArgumentCaptor<List<User>> userListCaptor = ArgumentCaptor.forClass(List.class);

        firebaseApiService.getAllUsers(mockCompletion);

        verify(mockCompletion).accept(userListCaptor.capture());

        List<User> capturedUsers = userListCaptor.getValue();
        assertEquals(2, capturedUsers.size());

        User user1 = capturedUsers.get(0);
        assertEquals("user1", user1.getId());
        assertEquals("user1@example.com", user1.getEmail());
        assertEquals("User One", user1.getName());
        assertEquals("https://example.com/user1.jpg", user1.getPhotoUrl());
        assertEquals(List.of("restaurant1", "restaurant2"), user1.getRestaurantLikeIDs());

        User user2 = capturedUsers.get(1);
        assertEquals("user2", user2.getId());
        assertEquals("user2@example.com", user2.getEmail());
        assertEquals("User Two", user2.getName());
        assertEquals("https://example.com/user2.jpg", user2.getPhotoUrl());
        assertEquals(List.of("restaurant3"), user2.getRestaurantLikeIDs());
    }

    @Test
    public void updateSelectedRestaurant_shouldUpdateWhenSelectedTrue() {
        Task<Void> updateTask = mock(Task.class);
        when(mockUserDocument.update(eq("selectedRestaurant"), any(Map.class)))
                .thenReturn(updateTask);

        doAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        }).when(updateTask).addOnSuccessListener(any(OnSuccessListener.class));

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = mockStatic(FirebaseAuth.class);
             MockedStatic<NotificationScheduler> notificationSchedulerMock = mockStatic(NotificationScheduler.class)) {

            firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(mockFirebaseAuth);
            when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockUser);

            firebaseApiService.updateSelectedRestaurant("resto123", true, mockContext);

            ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(mockUserDocument).update(eq("selectedRestaurant"), mapCaptor.capture());
            Map<String, Object> selectedRestaurantMap = mapCaptor.getValue();
            assertEquals("resto123", selectedRestaurantMap.get("id"));
            assertNotNull(selectedRestaurantMap.get("date"));

            verify(mockEditor, times(1)).putString("restaurantID", "resto123");
            verify(mockEditor, times(1)).apply();

            notificationSchedulerMock.verify(() -> NotificationScheduler.scheduleNotification(mockContext));
            notificationSchedulerMock.verify(() -> NotificationScheduler.cancelNotification(any()), never());
        }
    }

    @Test
    public void updateSelectedRestaurant_shouldUpdateWhenSelectedFalse() {
        Task<Void> updateTask = mock(Task.class);
        when(mockUserDocument.update(eq("selectedRestaurant"), any(Map.class)))
                .thenReturn(updateTask);

        doAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        }).when(updateTask).addOnSuccessListener(any(OnSuccessListener.class));

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = mockStatic(FirebaseAuth.class);
             MockedStatic<NotificationScheduler> notificationSchedulerMock = mockStatic(NotificationScheduler.class)) {

            firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(mockFirebaseAuth);
            when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockUser);

            firebaseApiService.updateSelectedRestaurant("resto123", false, mockContext);

            ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(mockUserDocument).update(eq("selectedRestaurant"), mapCaptor.capture());
            Map<String, Object> selectedRestaurantMap = mapCaptor.getValue();
            assertEquals("", selectedRestaurantMap.get("id"));
            assertNotNull(selectedRestaurantMap.get("date"));

            notificationSchedulerMock.verify(() -> NotificationScheduler.cancelNotification(mockContext));
            verify(mockEditor, times(1)).putString("restaurantID", "");
            verify(mockEditor, times(1)).apply();

            verify(mockEditor, never()).putString("restaurantID", "resto123");
            notificationSchedulerMock.verify(() -> NotificationScheduler.scheduleNotification(any()), never());
        }
    }

    @Test
    public void updateRestaurantLikes_whenIsLikedTrue_shouldCallUpdateWithArrayUnion() {
        Task<Void> updateTask = mock(Task.class);
        when(mockUserDocument.update(eq("restaurantLikeIDs"), any()))
                .thenReturn(updateTask);

        doAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        }).when(updateTask).addOnSuccessListener(any(OnSuccessListener.class));

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = mockStatic(FirebaseAuth.class)) {
            firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(mockFirebaseAuth);
            when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockUser);

            firebaseApiService.updateRestaurantLikes("restaurant123", true);

            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(mockUserDocument).update(eq("restaurantLikeIDs"), captor.capture());

            Object argument = captor.getValue();
            assertNotNull(argument);
            assertTrue(argument instanceof FieldValue);
            assertEquals("ArrayUnionFieldValue", argument.getClass().getSimpleName());
        }
    }

    @Test
    public void updateRestaurantLikes_whenIsLikedFalse_shouldCallUpdateWithArrayRemove() {
        Task<Void> updateTask = mock(Task.class);
        when(mockUserDocument.update(eq("restaurantLikeIDs"), any()))
                .thenReturn(updateTask);

        doAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        }).when(updateTask).addOnSuccessListener(any(OnSuccessListener.class));

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = mockStatic(FirebaseAuth.class)) {
            firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(mockFirebaseAuth);
            when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockUser);

            firebaseApiService.updateRestaurantLikes("restaurant123", false);

            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(mockUserDocument).update(eq("restaurantLikeIDs"), captor.capture());

            Object argument = captor.getValue();
            assertNotNull(argument);
            assertTrue(argument instanceof FieldValue);

            assertEquals("ArrayRemoveFieldValue", argument.getClass().getSimpleName());
        }
    }

    @Test
    public void getUserNamesInRestaurant_shouldReturnUserNames_whenTaskSuccessful() {
        Task<QuerySnapshot> mockQueryTask = mock(Task.class);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);

        when(mockFirestore.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.whereEqualTo(eq("selectedRestaurant.id"), eq("resto123")))
                .thenReturn(mockUsersCollection);
        when(mockUsersCollection.whereGreaterThanOrEqualTo(eq("selectedRestaurant.date"), any(Date.class)))
                .thenReturn(mockUsersCollection);
        when(mockUsersCollection.get()).thenReturn(mockQueryTask);

        DocumentSnapshot doc1 = mock(DocumentSnapshot.class);
        DocumentSnapshot doc2 = mock(DocumentSnapshot.class);
        when(doc1.getString("name")).thenReturn("Alice");
        when(doc2.getString("name")).thenReturn(null);
        List<DocumentSnapshot> docs = List.of(doc1, doc2);

        when(mockQuerySnapshot.iterator()).thenAnswer(invocation -> docs.iterator());
        when(mockQueryTask.isSuccessful()).thenReturn(true);
        when(mockQueryTask.getResult()).thenReturn(mockQuerySnapshot);
        doAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onComplete(mockQueryTask);
            return mockQueryTask;
        }).when(mockQueryTask).addOnCompleteListener(any());

        Consumer<List<String>> mockConsumer = mock(Consumer.class);
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);

        firebaseApiService.getUserNamesInRestaurant("resto123", mockConsumer);

        verify(mockConsumer).accept(captor.capture());
        List<String> result = captor.getValue();
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0));
    }

    @Test
    public void getUserNamesInRestaurant_shouldReturnEmptyList_whenTaskFails() {
        Task<QuerySnapshot> mockQueryTask = mock(Task.class);

        when(mockFirestore.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.whereEqualTo(eq("selectedRestaurant.id"), eq("resto123")))
                .thenReturn(mockUsersCollection);
        when(mockUsersCollection.whereGreaterThanOrEqualTo(eq("selectedRestaurant.date"), any(Date.class)))
                .thenReturn(mockUsersCollection);
        when(mockUsersCollection.get()).thenReturn(mockQueryTask);

        when(mockQueryTask.isSuccessful()).thenReturn(false);
        doAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onComplete(mockQueryTask);
            return mockQueryTask;
        }).when(mockQueryTask).addOnCompleteListener(any());

        Consumer<List<String>> mockConsumer = mock(Consumer.class);
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);

        firebaseApiService.getUserNamesInRestaurant("resto123", mockConsumer);

        verify(mockConsumer).accept(captor.capture());
        List<String> result = captor.getValue();
        assertTrue(result.isEmpty());
    }

}



