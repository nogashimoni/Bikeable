package com.nnys.bikeable;

import com.google.maps.android.PolyUtil;
import com.google.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Sharon on 23/11/2015.
 */
public class MapUtils {

    public static LatLng getModelLatLngFromGms(com.google.android.gms.maps.model.LatLng gmsLatLng){
        LatLng modelLatLng = new LatLng(gmsLatLng.latitude, gmsLatLng.longitude);
        return modelLatLng;
    }

    public static com.google.android.gms.maps.model.LatLng getGmsLatLngFromModel(LatLng modelLatLng){
        com.google.android.gms.maps.model.LatLng gmsLatLng =
                new com.google.android.gms.maps.model.LatLng(modelLatLng.lat, modelLatLng.lng);
        return gmsLatLng;
    }

    public static ArrayList<LatLng> getArrModelLatLngFromGms (ArrayList<com.google.android.gms.maps.model.LatLng> gmsLatLngArr){
        ArrayList<LatLng> result = new ArrayList<>();
        for (com.google.android.gms.maps.model.LatLng gmsLatLng : gmsLatLngArr){
            result.add(getModelLatLngFromGms(gmsLatLng));
        }
        return result;
    }

    public static ArrayList<com.google.android.gms.maps.model.LatLng> getArrGmsLatLngFromModel (ArrayList<LatLng> modelLatLngArr){
        ArrayList<com.google.android.gms.maps.model.LatLng> result = new ArrayList<>();
        for (LatLng modelLatLng : modelLatLngArr){
            result.add(getGmsLatLngFromModel(modelLatLng));
        }
        return result;
    }

    public static void selectClickedRoute (DirectionsManager directionsManager, com.google.android.gms.maps.model.LatLng clickLatLng){
        for (int i = 0; i < directionsManager.getNumRoutes(); i++) {
            if (PolyUtil.isLocationOnPath(clickLatLng, directionsManager.getRoutesPolylineOpts()
                    .get(i).getPoints(), true, 100)){
                directionsManager.setSelectedRouteIndex(i);
                directionsManager.selectAndColorRoute(directionsManager.getSelectedRouteIndex());
                break;
            }
        }
    }

}
