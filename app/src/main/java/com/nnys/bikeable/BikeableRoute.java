package com.nnys.bikeable;

import android.renderscript.Sampler;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

import java.util.ArrayList;

public class BikeableRoute {

    public final static int GRAPH_X_INTERVAL = 20;
    public final static int MAX_GRAPH_SAMPLES = 400;

    /* route's DirectionRout object */
    DirectionsRoute directionsRoute;

    /* route polyline reprs */
    PolylineOptions routePolylineOptions;
    Polyline routePolyline;
    EncodedPolyline routeEncodedPolyline;

    /* route distance from source to destination point */
    long distance;

    /* route's elevation info */
    PathElevationQuerier elevationQuerier;
    ElevationResult[] routeElevationArr;
    int numOfElevationSamples;
    double bikePathPercentage;

    boolean isBikePathPolylinesAdded;
    boolean isBikePathShown;
    ArrayList <PolylineOptions> bikePathInRoute;
    ArrayList <Polyline> bikePathPolyLineInRoute;

    /* BikeableRoute constructor */
    public BikeableRoute(DirectionsRoute directionsRoute, GoogleMap mMap) {
        this.directionsRoute = directionsRoute;

        distance = calcRouteDistance();

        elevationQuerier = new PathElevationQuerier(this.directionsRoute.overviewPolyline);
        numOfElevationSamples = calcNumOfSamples();
        routeElevationArr = createGraphElevationArray();

        routePolylineOptions = createRoutesPolyOpts();
        routePolyline = mMap.addPolyline(routePolylineOptions); // draws the polyline on map
        if (IriaData.isDataReceived) {
            BikePathCalculator pathCalculator = new BikePathCalculator(routePolylineOptions, IriaData.getBikePathsTLVPolyLineOpt(),
                    directionsRoute);
            bikePathPercentage = pathCalculator.getBikePathPercentageByRoute();
            bikePathInRoute = pathCalculator.getBikePaths();
            addBikePathToMap(mMap);
        }
    }

    public void addBikePathToMap (GoogleMap mMap) {
        if (isBikePathPolylinesAdded)
            return;
        Log.i("info:", "inside add function");
        bikePathPolyLineInRoute = new ArrayList<>();
        for (PolylineOptions line : bikePathInRoute) {
            line.visible(false);
            Log.i("info", "inside add function for");
            bikePathPolyLineInRoute.add(mMap.addPolyline(line));
        }
        isBikePathPolylinesAdded = true;
    }

    public void showBikePathOnMap() {
        if (!isBikePathPolylinesAdded){
            return;
        }
        Log.i("info", "inside show function");
        for (Polyline line : bikePathPolyLineInRoute){
            Log.i("info", "inside show function for");
            line.setVisible(true);
            line.setZIndex(10);
        }
        isBikePathShown = true;
    }

    public void removeBikePathFromMap() {
        if (!isBikePathPolylinesAdded) {
            return;
        }
        for (Polyline line : bikePathPolyLineInRoute) {
            line.setVisible(false);
        }
        isBikePathShown = false;
    }


    private ElevationResult[] createGraphElevationArray() {
        routeElevationArr = elevationQuerier.getElevationSamples(numOfElevationSamples);
        return routeElevationArr;
    }

    private int calcNumOfSamples(){
        int result = elevationQuerier.calcNumOfSamplesForXmetersIntervals(distance, GRAPH_X_INTERVAL, MAX_GRAPH_SAMPLES);
        return result;
    }

    private long calcRouteDistance() {
        long distance = 0;
        for (DirectionsLeg leg : directionsRoute.legs) {
            distance += leg.distance.inMeters;
        }
        return distance;
    }

    private PolylineOptions createRoutesPolyOpts() {
        com.google.android.gms.maps.model.LatLng currPoint;
        PolylineOptions line = new PolylineOptions();
        for (LatLng point : directionsRoute.overviewPolyline.decodePath()) {
            currPoint = new com.google.android.gms.maps.model.LatLng(point.lat, point.lng);
            line.add(currPoint);
        }
        return line;
    }

    public boolean isBikePathShown (){
        return isBikePathShown;
    }

}
