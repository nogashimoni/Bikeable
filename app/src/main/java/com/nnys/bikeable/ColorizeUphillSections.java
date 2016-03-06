package com.nnys.bikeable;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.model.ElevationResult;

import java.util.ArrayList;

/**
 * This class creates a list of sections in a BikeableRoute, that have a significant uphill slope.
 */
public class ColorizeUphillSections {

    public final static int SIGNIFICANT_UPHILL_DEGREE = 1;
    BikeableRoute bikeableRoute;
    ElevationResult[] routeElevationArr;
    ArrayList<PolylineOptions> uphillSections;
    ArrayList<Polyline> uphillPolylines;
    private boolean isUphillPolylinesAdded;
    private boolean isUphillSectionsShowen;

    public ColorizeUphillSections(BikeableRoute bikeableRoute){
        Log.i("Info:", "Init ColorizeUphillSections");
        this.bikeableRoute = bikeableRoute;
        this.routeElevationArr = bikeableRoute.getRouteElevationArr();
        this.uphillSections = getUphillSectionsAsBikeableRoutePolylineOptions();
    }

    private ArrayList<PolylineOptions> getUphillSectionsAsBikeableRoutePolylineOptions() {
        Log.i("Info:", "PolylineOptions");
        ArrayList<PolylineOptions> uphillSections = new ArrayList<>();
        double[] degreesArray = this.bikeableRoute.getDegreesArray();
        for (int i=0; i < degreesArray.length ; i++) {
            if (degreesArray[i] >= SIGNIFICANT_UPHILL_DEGREE){
                Log.i("INFO:", String.format("currPathPolylineOpts!!! %f", degreesArray[i]));
                addUphillSectionToSections(i, uphillSections);
            }
        }
        return uphillSections;
    }

    private void addUphillSectionToSections(int i, ArrayList<PolylineOptions> uphillSections) {
        Log.i("Info:", "addUphillSectionToSections");
        PolylineOptions currPathPolylineOpts = new PolylineOptions();
        currPathPolylineOpts.color(Color.RED);
        currPathPolylineOpts.add(new LatLng(routeElevationArr[i].location.lat, routeElevationArr[i].location.lng));
        currPathPolylineOpts.add(new LatLng(routeElevationArr[i + 1].location.lat, routeElevationArr[i + 1].location.lng));
        uphillSections.add(currPathPolylineOpts);
    }

    public void addUphillSectionsToMap(GoogleMap mMap) {
        Log.i("Info:", "addUphillSectionsToMap");
        uphillPolylines = new ArrayList<>();
        for (PolylineOptions line : uphillSections) {
            line.visible(false);
            uphillPolylines.add(mMap.addPolyline(line));
        }
    }

    public void showUphillSectionsToMap(){
        Log.i("Info:", "showUphillSectionsToMap");
        for (Polyline line : uphillPolylines){
            line.setVisible(true);
            line.setZIndex(10);
        }
        isUphillPolylinesAdded = true;
    }

    public void removeUphillSectionsFromMap(){
        Log.i("Info:", "removeUphillSectionsFromMap");
        if (!isUphillPolylinesAdded){
            return;
        }
        for (Polyline line : uphillPolylines) {
            line.setVisible(false);
            line.remove();
        }
        uphillPolylines.removeAll(uphillPolylines);
        uphillSections.removeAll(uphillSections);
        isUphillPolylinesAdded = false;
    }

    public void hideUphillSectionsFromMap(){
        if (!isUphillPolylinesAdded){
            return;
        }
        for (Polyline line : uphillPolylines) {
            line.setVisible(false);
        }
    }
}
