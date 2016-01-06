package com.nnys.bikeable;

import android.graphics.Color;

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


    static ArrayList<Polyline> bikePathsPolylines;
    static boolean isBikePathPolylinesAdded;

    public ColorizeUphillSections(BikeableRoute bikeableRoute){
        this.bikeableRoute = bikeableRoute;
        this.uphillSections = getUphillSectionsAsBikeableRoutePolylineOptions();
        this.routeElevationArr = bikeableRoute.getRouteElevationArr();
    }

    private ArrayList<ArrayList<PolylineOptions>> getUphillSectionsAsBikeableRoutePolylineOptions() {

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
        ArrayList<PolylineOptions> significantUphillSection = new ArrayList<>();
        PolylineOptions currPathPolylineOpts = new PolylineOptions();
        currPathPolylineOpts.color(Color.RED);
        currPathPolylineOpts.add(new LatLng(routeElevationArr[i].location.lat, routeElevationArr[i].location.lng));
        currPathPolylineOpts.add(new LatLng(routeElevationArr[i+1].location.lat, routeElevationArr[i+1].location.lng));
        significantUphillSection.add(currPathPolylineOpts);
        uphillSections.add(significantUphillSection);
    }

    public void addUphillSectionsToMap(GoogleMap mMap) {
        if (isBikePathPolylinesAdded)
            return;
        bikePathsPolylines = new ArrayList<>();
        for (ArrayList<PolylineOptions> uphillSection: uphillSections) {
            for (PolylineOptions line : uphillSection) {
                line.zIndex(10); // TODO: not hard coded
//                line.width(5); // TODO: not hard coded
                line.visible(false);
                bikePathsPolylines.add(mMap.addPolyline(line));
            }
        }
        isBikePathPolylinesAdded = true;
    }

    public void showUphillSectionsToMap(){
        for (Polyline line : bikePathsPolylines){
            line.setVisible(true);
        }
    }

    public void removeUphillSectionsFromMap(){
        for (Polyline line : bikePathsPolylines) {
            line.remove();
        }
    }

    public void hideUphillSectionsFromMap(){
        for (Polyline line : bikePathsPolylines) {
            line.setVisible(false);
        }
    }
}
