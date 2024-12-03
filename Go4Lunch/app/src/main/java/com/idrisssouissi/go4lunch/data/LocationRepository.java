package com.idrisssouissi.go4lunch.data;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

public class LocationRepository {


    private MutableLiveData<LatLng> lastLocation = new MutableLiveData<>();

    public void setLastLocation(LatLng location) {
        lastLocation.postValue(location);
    }

    public MutableLiveData<LatLng> getLastLocation() {
        return lastLocation;
    }

}
