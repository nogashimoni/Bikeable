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

    public CurrentLocationQuerier(Context callingActivityContext) {
        this.callingActivityContext = callingActivityContext;
        this.setCurrentLocationListener();
    }

    private void  setCurrentLocationListener() {
        currentLocationLang = -1;
        currentLocationLat = -1;
        locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                currentLocationLat = location.getLatitude();
                currentLocationLang = location.getLongitude();
                try {
                    sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private boolean isCurrentLocationSet(){
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this.callingActivityContext, locationResult);
        return !(currentLocationLang == -1 || currentLocationLat == -1);
    }


    public LatLng getCurrentLocationLatLang(LatLng latLng) {
        Log.i("INFO", "in get current loctaion lat lang");
        while (!isCurrentLocationSet()) {
            try {

                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        latLng.lat = currentLocationLat;
        latLng.lng = currentLocationLang;
        currentLocationLat = -1 ;
        currentLocationLang = -1;
        return latLng;
    }

}
