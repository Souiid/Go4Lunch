package com.idrisssouissi.go4lunch.data;

import com.google.firebase.firestore.FirebaseFirestore;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class RepositoryModule {

    @Provides
    @Singleton
    RestaurantRepository provideRestaurantRepository(RestaurantApiService apiService) {
        return new RestaurantRepository(apiService);
    }

    @Provides
    @Singleton
    UserRepository provideUserRepository(FirebaseApiService apiService) {
        return new UserRepository(apiService);
    }

    @Provides
    @Singleton
    LocationRepository provideLocationRepository() {
        return new LocationRepository();
    }

    @Provides
    @Singleton
    FirebaseApiService provideFirebaseApiService() {
        return  new FirebaseApiService(
                FirebaseFirestore.getInstance(),
                new FirebaseAuthProviderImpl()
        );
    }

    @Provides
    RestaurantApiService provideRestaurantApiService() {
        return new RestaurantApiService();
    }
}