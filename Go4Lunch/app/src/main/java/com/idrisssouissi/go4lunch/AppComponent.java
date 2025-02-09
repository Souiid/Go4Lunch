package com.idrisssouissi.go4lunch;

import com.idrisssouissi.go4lunch.data.RepositoryModule;
import com.idrisssouissi.go4lunch.ui.HomeActivity;
import com.idrisssouissi.go4lunch.ui.HomeViewModel;
import com.idrisssouissi.go4lunch.ui.MainActivity;
import com.idrisssouissi.go4lunch.ui.RestaurantDetailsActivity;
import com.idrisssouissi.go4lunch.ui.RestaurantDetailsViewModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {RepositoryModule.class})
public interface AppComponent {

    void inject(MainActivity activity);
    void inject(HomeActivity homeActivity);
    void inject(RestaurantDetailsActivity restaurantDetailsActivity);

    HomeViewModel.Factory provideHometViewModelFactory();
    RestaurantDetailsViewModel.Factory provideRestaurantDetailsViewModelFactory();

}
