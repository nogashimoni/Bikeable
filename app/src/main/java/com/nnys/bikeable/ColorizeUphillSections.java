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
 * Created by nleibovich on 01/01/2016.
 * This class creates a list of sections in a BikeableRoute, that have a significant uphill slope.
 */
public class ColorizeUphillSections {

    public final static int SIGNIFICANT_UPHILL_DEGREE = 5;
    BikeableRoute bikeableRoute;
    ArrayList<ArrayList<PolylineOptions>> uphillSections;
    ElevationResult[] routeElevationArr;


    static ArrayList<Polyline> uphillPolylines;
    static boolean isBikePathPolylinesAdded;

    public ColorizeUphillSections(BikeableRoute bikeableRoute){
        Log.i("Info:", "ColorizeUphillSections");
        this.bikeableRoute = bikeableRoute;
        this.routeElevationArr = bikeableRoute.getRouteElevationArr();
        this.uphillSections = getUphillSectionsAsBikeableRoutePolylineOptions();
    }

    private ArrayList<ArrayList<PolylineOptions>> getUphillSectionsAsBikeableRoutePolylineOptions() {
        Log.i("Info:", "getUphillSectionsAsBikeableRoutePolylineOptions");
        ArrayList<ArrayList<PolylineOptions>> uphillSections = new ArrayList<>();
        double[] degreesArray = this.bikeableRoute.getDegreesArray();
        for (int i=0; i < degreesArray.length ; i++) {
            if (degreesArray[i] >= SIGNIFICANT_UPHILL_DEGREE){
                addUphillSectionToSections(i);
            }
        }
        return uphillSections;
    }

    private void addUphillSectionToSections(int i) {
        Log.i("Info:", "addUphillSectionToSections");
        ArrayList<PolylineOptions> significantUphillSection = new ArrayList<>();
        PolylineOptions currPathPolylineOpts = new PolylineOptions();
        currPathPolylineOpts.color(Color.RED);
        currPathPolylineOpts.add(new LatLng(routeElevationArr[i].location.lat, routeElevationArr[i].location.lng));
        currPathPolylineOpts.add(new LatLng(routeElevationArr[i+1].location.lat, routeElevationArr[i+1].location.lng));
        significantUphillSection.add(currPathPolylineOpts);
        uphillSections.add(significantUphillSection);
    }

    public void addUphillSectionsToMap(GoogleMap mMap) {
        Log.i("Info:", "addUphillSectionsToMap");
        if (isBikePathPolylinesAdded)
            return;
        uphillPolylines = new ArrayList<>();
        for (ArrayList<PolylineOptions> uphillSection: uphillSections) {
            for (PolylineOptions line : uphillSection) {
                line.zIndex(10); // TODO: not hard coded
//                line.width(5); // TODO: not hard coded
                line.visible(true);
                uphillPolylines.add(mMap.addPolyline(line));
            }
        }
        isBikePathPolylinesAdded = true;
    }

//    public void showUphillSectionsToMap(){
//        if (!isBikePathPolylinesAdded){
//            return;
//        }
//        for (Polyline line : uphillPolylines){
//            line.setVisible(true);
//        }
//    }

    public void removeUphillSectionsFromMap(){
        Log.i("Info:", "removeUphillSectionsFromMap");
        if (!isBikePathPolylinesAdded){
            return;
        }
        for (Polyline line : uphillPolylines) {
            line.remove();
        }
    }

//    public void hideUphillSectionsFromMap(){
//        if (!isBikePathPolylinesAdded){
//            return;
//        }
//        for (Polyline line : uphillPolylines) {
//            line.setVisible(false);
//        }
//    }
}
