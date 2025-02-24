package com.idrisssouissi.go4lunch;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.idrisssouissi.go4lunch.data.FirebaseApiService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseApiServiceTest  {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private DocumentReference mockDocRef;

    @Mock
    private CollectionReference mockCollectionRef;

    @Mock
    private Query mockQuery;

    @Mock
    private Task<QuerySnapshot> mockTask;


    @Mock
    private QuerySnapshot mockQuerySnapshot;

    @Mock
    private SharedPreferences mockSharedPreferences;

    @Mock
    private SharedPreferences.Editor mockEditor;

    @Mock
    private Context mockContext;

    @Mock
    private Task<Void> mockVoidTask;


    private FirebaseApiService firebaseApiService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock FirebaseAuth
        mockStatic(FirebaseAuth.class);
        when(FirebaseAuth.getInstance()).thenReturn(mockAuth);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("testUserId");
        when(mockUser.getPhotoUrl()).thenReturn(mock(Uri.class));


        // Mock FirebaseFirestore
        mockStatic(FirebaseFirestore.class);
        when(FirebaseFirestore.getInstance()).thenReturn(mockFirestore);
        when(mockFirestore.collection("users")).thenReturn(mockCollectionRef);
        when(mockCollectionRef.document(anyString())).thenReturn(mockDocRef);
        when(mockCollectionRef.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.whereGreaterThanOrEqualTo(anyString(), any())).thenReturn(mockQuery);

        when(mockDocRef.update(anyString(), any())).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            ((OnSuccessListener<Void>) invocation.getArgument(0)).onSuccess(null);
            return mockVoidTask;
        });
        when(mockVoidTask.addOnFailureListener(any())).thenAnswer(invocation -> mockVoidTask);

        // Mock Task et QuerySnapshot
        doReturn(mockTask).when(mockQuery).get();
        doReturn(true).when(mockTask).isSuccessful();
        doReturn(mockQuerySnapshot).when(mockTask).getResult();

        // Mock update()
        when(mockDocRef.update(anyString(), any())).thenReturn(mock(Task.class));



        // Instancier FirebaseApiService avec Firestore mocké
        firebaseApiService = new FirebaseApiService();
        firebaseApiService.db = mockFirestore;
    }

    @Test
    public void testCreateUserInFirestore() {
        // Simuler l'ajout d'un utilisateur dans Firestore
        Task<Void> mockTask = mock(Task.class);
        when(mockDocRef.set(anyMap(), any(SetOptions.class))).thenReturn(mockTask);

        firebaseApiService.createUserInFirestore(mockUser, () -> {});

        // Vérifier que la méthode set() a bien été appelée
        verify(mockDocRef).set(anyMap(), any(SetOptions.class));
    }

    @Test
    public void testIsUserConnected() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        assertTrue(firebaseApiService.isUserConnected());

        when(mockAuth.getCurrentUser()).thenReturn(null);
        assertFalse(firebaseApiService.isUserConnected());
    }

    @Test
    public void testGetAllUsers() {
        // Simuler les documents retournés
        DocumentSnapshot mockDocument = mock(DocumentSnapshot.class);
        List<DocumentSnapshot> mockDocuments = Collections.singletonList(mockDocument);

        Task<QuerySnapshot> mockTask = mock(Task.class);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);

        when(mockCollectionRef.get()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshot.getDocuments()).thenReturn(mockDocuments);

        when(mockDocument.getId()).thenReturn("userId123");
        when(mockDocument.getString("email")).thenReturn("test@example.com");
        when(mockDocument.getString("name")).thenReturn("Test User");
        when(mockDocument.getString("photoUrl")).thenReturn("mock_url");
        when(mockDocument.get("selectedRestaurant")).thenReturn(new HashMap<>());
        when(mockDocument.get("restaurantLikeIDs")).thenReturn(new ArrayList<>());

        firebaseApiService.getAllUsers(users -> {
            assertNotNull(users);
            assertEquals(1, users.size());
            assertEquals("Test User", users.get(0).getName());
        });

        verify(mockCollectionRef).get();
    }


    @Test
    public void testGetUserNamesInRestaurant() {
        DocumentSnapshot mockDocument = mock(DocumentSnapshot.class);
        List<DocumentSnapshot> mockDocuments = Arrays.asList(mockDocument);

        when(mockQuerySnapshot.getDocuments()).thenReturn(mockDocuments);
        when(mockDocument.getString("name")).thenReturn("Test User");

        firebaseApiService.getUserNamesInRestaurant("restaurant123", names -> {
            assertNotNull(names);
            assertEquals(1, names.size());
            assertEquals("Test User", names.get(0));
        });

        verify(mockQuery).get();
    }

    @Test
    public void testSignOut() {
        firebaseApiService.signOut();
        verify(mockAuth).signOut();
    }

    @After
    public void tearDown() {
        // Libérer les mocks statiques après chaque test
        clearAllCaches();
    }

}