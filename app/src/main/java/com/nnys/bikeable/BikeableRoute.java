package com.nnys.bikeable;

import android.renderscript.Sampler;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    /* BikeableRoute constructor */
    public BikeableRoute(DirectionsRoute directionsRoute, GoogleMap mMap) {
        this.directionsRoute = directionsRoute;

        distance = calcRouteDistance();

        elevationQuerier = new PathElevationQuerier(this.directionsRoute.overviewPolyline);
        numOfElevationSamples = calcNumOfSamples();
        routeElevationArr = createGraphElevationArray();

        routePolylineOptions = createRoutesPolyOpts();
        routePolyline = mMap.addPolyline(routePolylineOptions); // draws the polyline on map
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

    public List<LatLng> getRouteLatLngs(){
        return directionsRoute.overviewPolyline.decodePath();
    }

}
