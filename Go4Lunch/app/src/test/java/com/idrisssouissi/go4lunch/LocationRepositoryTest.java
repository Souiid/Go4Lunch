package com.idrisssouissi.go4lunch;

import static org.junit.Assert.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.model.LatLng;
import com.idrisssouissi.go4lunch.data.LocationRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class LocationRepositoryTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private LocationRepository locationRepository;

    @Before
    public void setUp() {
        locationRepository = new LocationRepository();
    }

    @Test
    public void testSetLastLocation_updatesLiveData() {
        // Arrange
        LatLng expectedLocation = new LatLng(40.7128, -74.0060);

        // Act
        locationRepository.setLastLocation(expectedLocation);

        // Assert
        assertEquals(expectedLocation, locationRepository.getLastLocation().getValue());
    }

    @Test
    public void testObserverReceivesUpdates() {
        // Arrange
        Observer<LatLng> observer = Mockito.mock(Observer.class);
        locationRepository.getLastLocation().observeForever(observer);

        LatLng newLocation = new LatLng(48.8566, 2.3522); // Paris

        // Act
        locationRepository.setLastLocation(newLocation);

        // Assert
        Mockito.verify(observer).onChanged(newLocation);

        // Cleanup
        locationRepository.getLastLocation().removeObserver(observer);
    }
}
