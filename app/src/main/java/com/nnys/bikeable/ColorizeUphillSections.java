package com.nnys.bikeable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by nleibovich on 01/01/2016.
 * This class creates a list of sections in a BikeableRoute, that have a significant uphill slope.
 */
public class ColorizeUphillSections {

    public final static int SIGNIFICANT_UPHILL_DEGREE = 5;
    BikeableRoute bikeableRoute;
    ArrayList<BikeableRoute> uphillSections;
    private static ArrayList<PolylineOptions> bikePathPolylinesOpts;


    static ArrayList<Polyline> bikePathsPolylines;
    static boolean isBikePathPolylinesAdded;
    static boolean isBikePathShown;

    public ColorizeUphillSections(BikeableRoute bikeableRoute){
        this.bikeableRoute = bikeableRoute;
        this.uphillSections = getUphillSectionsAsBikeableRoute();
    }

    private ArrayList<BikeableRoute> getUphillSectionsAsBikeableRoute() {

        ArrayList<BikeableRoute> uphillSections = new ArrayList<>();
        double[] degreesArray = this.bikeableRoute.getDegreesArray();
        for (int i=0; i < degreesArray.length ; i++) {
            if (degreesArray[i] >= SIGNIFICANT_UPHILL_DEGREE){
                addUphillSection(uphillSections, i);
            }
        }
        return uphillSections;
    }

    private void addUphillSection(ArrayList<BikeableRoute> uphillSections, int i) {
        //TODO
    }

    private ArrayList<PolylineOptions> convertBikeableRouteToPolylineOptions(){
        //TODO
        return bikePathPolylinesOpts;
    }

    public static void addBikePathToMap (GoogleMap mMap) {
        if (isBikePathPolylinesAdded)
            return;
        bikePathsPolylines = new ArrayList<>();
        for (PolylineOptions line : bikePathPolylinesOpts) {
            line.zIndex(20); // TODO: not hard coded
            line.width(5); // TODO: not hard coded
            line.visible(false);
            bikePathsPolylines.add(mMap.addPolyline(line));
        }
        isBikePathPolylinesAdded = true;
    }

    public static void showBikePathOnMap() {
        if (!isBikePathPolylinesAdded){
            return;
        }
        for (Polyline line : bikePathsPolylines){
            line.setVisible(true);
        }
        isBikePathShown = true;
    }

    public static void removeBikePathFromMap() {
        if (!isBikePathPolylinesAdded) {
            return;
        }
        for (Polyline line : bikePathsPolylines) {
            line.setVisible(false);
        }
        isBikePathShown = false;
    }

    public static boolean isBikePathShown(){
        return isBikePathShown;
    }
}
