package com.nnys.bikeable;
import android.content.Context;
import com.google.maps.model.LatLng;
import static java.lang.Thread.sleep;
import android.location.Location;
import android.util.Log;

public class CurrentLocationQuerier {

    private double currentLocationLat;
    private double currentLocationLang;
    private MyLocation.LocationResult locationResult;
    private Context callingActivityContext;
    private MyLocation myLocation;

    public CurrentLocationQuerier(Context callingActivityContext) {
        this.callingActivityContext = callingActivityContext;
        this.currentLocationLat = -1;
        this.currentLocationLang = -1;
        this.myLocation = new MyLocation();
        locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                currentLocationLat = location.getLatitude();
                currentLocationLang = location.getLongitude();
            }
        };
    }


    private double getCurrentLocationLat() {
        return currentLocationLat;
    }

    private double getCurrentLocationLang() {
        return currentLocationLang;
    }

    public LatLng waitForCurrentLocation() {
        while (getCurrentLocationLang() == - 1 || getCurrentLocationLang() == -1) {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LatLng result = new LatLng(currentLocationLat, currentLocationLang);
        currentLocationLang = -1;
        currentLocationLat = -1;
        return result;
    }

    public LatLng getCurrentLocationDontWait() {
        LatLng result = new LatLng(currentLocationLat, currentLocationLang);
        currentLocationLang = -1;
        currentLocationLang = -1;
        return result;
    }

    public void askToGetCurrentLocation() {
        myLocation.getLocation(this.callingActivityContext, locationResult);
    }

}
