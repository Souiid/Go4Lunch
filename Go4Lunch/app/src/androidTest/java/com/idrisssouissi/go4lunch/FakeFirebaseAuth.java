package com.idrisssouissi.go4lunch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.interop.InteropAppCheckTokenProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.heartbeatinfo.HeartBeatController;
import com.google.firebase.inject.Provider;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

public class FakeFirebaseAuth extends FirebaseAuth {

    public FakeFirebaseAuth(@NonNull FirebaseApp firebaseApp, @NonNull Provider<InteropAppCheckTokenProvider> provider, @NonNull Provider<HeartBeatController> provider1, @NonNull Executor executor, @NonNull Executor executor1, @NonNull Executor executor2, @NonNull ScheduledExecutorService scheduledExecutorService, @NonNull Executor executor3) {
        super(firebaseApp, provider, provider1, executor, executor1, executor2, scheduledExecutorService, executor3);
    }

    @Override
    public FirebaseUser getCurrentUser() {
        FirebaseUser mockUser = mock(FirebaseUser.class);
        when(mockUser.getUid()).thenReturn("fake_user_id");
        return mockUser;
    }
}
