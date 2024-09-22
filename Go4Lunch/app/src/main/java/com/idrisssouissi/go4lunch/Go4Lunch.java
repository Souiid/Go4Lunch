package com.idrisssouissi.go4lunch;

import android.app.Application;

public class Go4Lunch extends Application {
    private static AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent.builder().build();
    }

    public static AppComponent getAppComponent() {
        return appComponent;
    }
}
