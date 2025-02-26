package com.idrisssouissi.go4lunch;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import android.net.Uri;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseApiServiceTest {

    @Mock private FirebaseFirestore mockFirestore;
    @Mock private CollectionReference mockUsersCollection;
    @Mock private DocumentReference mockUserDocument;
    @Mock private Task<Void> mockTask;
    @Mock private Task<QuerySnapshot> mockQueryTask;
    @Mock private QuerySnapshot mockQuerySnapshot;
    @Mock private DocumentSnapshot mockDocument1;
    @Mock private DocumentSnapshot mockDocument2;
    @Mock private Consumer<List<User>> mockCompletion;
    @Mock private FirebaseUser mockUser;
    @Mock private FirebaseAuthProvider mockAuthProvider;


    private FirebaseApiService firebaseApiService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // 🔹 Mock Firestore
        when(mockFirestore.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.document(anyString())).thenReturn(mockUserDocument);
        when(mockUserDocument.set(any(Map.class), any(SetOptions.class))).thenReturn(mockTask);

        // 🔹 Mock FirebaseUser
        when(mockUser.getUid()).thenReturn("user123");
        when(mockUser.getDisplayName()).thenReturn("Test User");

        // ✅ 🔥 Correction : Mock de Uri.parse() pour éviter le NullPointerException
        Uri mockUri = mock(Uri.class);
        when(mockUri.toString()).thenReturn("https://example.com/user1.jpg");
        when(mockUser.getPhotoUrl()).thenReturn(mockUri);

        when(mockUser.getEmail()).thenReturn("test@example.com");

        // ✅ 🔥 Correction : s'assurer que le mock est bien terminé
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);

        // 🔹 Initialise FirebaseApiService
        firebaseApiService = new FirebaseApiService(mockFirestore, mockAuthProvider);
    }


    @Test
    public void createUserInFirestore_shouldAddUserToFirestoreAndRunCompletion() {
        // 🔹 Simuler un succès Firestore
        ArgumentCaptor<OnSuccessListener<Void>> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        when(mockTask.addOnSuccessListener(successCaptor.capture())).thenReturn(mockTask);

        // 🔹 Exécution
        firebaseApiService.createUserInFirestore(mockUser, () -> mockCompletion.accept(null));

        // 🔹 Vérification des données envoyées à Firestore
        ArgumentCaptor<Map<String, Object>> userDataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockUserDocument).set(userDataCaptor.capture(), eq(SetOptions.merge()));

        Map<String, Object> capturedData = userDataCaptor.getValue();
        assertEquals("Test User", capturedData.get("name"));
        assertEquals("test@example.com", capturedData.get("email"));
        assertTrue(capturedData.containsKey("selectedRestaurant"));
        assertTrue(capturedData.containsKey("restaurantLikeIDs"));

        // 🔹 Simuler un succès et vérifier que completion est bien appelé
        successCaptor.getValue().onSuccess(null);
        verify(mockCompletion).accept(null);
    }
}



