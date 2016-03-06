package com.nnys.bikeable;

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
import java.util.List;

/**
 * This class manages each route that is saved on allRoutes bikeableRoutes list.
 */
public class BikeableRoute {

    public final static int GRAPH_X_INTERVAL = 20;
    public final static int MAX_GRAPH_SAMPLES = 400;
    public final static int WALKING_SPEED = 5;
    public final static int BIKIG_SPEED = 15;


    /* route's DirectionRout object */
    DirectionsRoute directionsRoute;

    /* route polyline reprs */
    PolylineOptions routePolylineOptions;
    Polyline routePolyline;
    EncodedPolyline routeEncodedPolyline;
    PathElevationScoreCalculator pathElevationScoreCalculator;

    /* route distance from source to destination point */
    long distance;
    long duration;
    String durationString;
    String distanceString;


    /* route's elevation info */
    PathElevationQuerier elevationQuerier;
    ElevationResult[] routeElevationArr;
    double averageUphillDegree;
    int numOfElevationSamples;
    double worstDegree;
    double pathElevationScore;

    double bikePathPercentage;
    boolean isBikePathPolylinesAdded;
    boolean isBikePathShown;
    ArrayList <PolylineOptions> bikePathInRoute;
    ArrayList <Polyline> bikePathPolyLineInRoute;
    ColorizeUphillSections colorizeUphillSections;

    double algorithmScore;

    /* BikeableRoute constructor */
    public BikeableRoute(DirectionsRoute directionsRoute, GoogleMap mMap, long distance) {
        this.directionsRoute = directionsRoute;

        this.distance = distance;
        duration = calculateEstimatedBikingDuration();
        durationString = updateDurationString();
        distanceString = updateDistanceString();

        elevationQuerier = new PathElevationQuerier(this.directionsRoute.overviewPolyline);
        numOfElevationSamples = calcNumOfSamples();
        routeElevationArr = createGraphElevationArray();
        pathElevationScoreCalculator = new PathElevationScoreCalculator(routeElevationArr, distance);
        averageUphillDegree = pathElevationScoreCalculator.getAvregeUphillDegree();
        worstDegree = pathElevationScoreCalculator.calcWorstDegree();
        pathElevationScore = pathElevationScoreCalculator.getPathScore();

        routePolylineOptions = createRoutesPolyOpts();
        routePolyline = mMap.addPolyline(routePolylineOptions); // draws the polyline on map
        colorizeUphillSections = new ColorizeUphillSections(this);
        colorizeUphillSections.addUphillSectionsToMap(mMap);
        if (IriaData.isDataReceived) {
            BikePathCalculator pathCalculator = new BikePathCalculator(routePolylineOptions, IriaData.getBikePathsTLVPolyLineOpt(),
                    directionsRoute);
            bikePathPercentage = pathCalculator.getBikePathPercentageByRoute();
            bikePathInRoute = pathCalculator.getBikePaths();
            addBikePathToMap(mMap);
        }

        algorithmScore = 0;
    }

    public void addBikePathToMap(GoogleMap mMap) {
        if (isBikePathPolylinesAdded)
            return;
        bikePathPolyLineInRoute = new ArrayList<>();
        for (PolylineOptions line : bikePathInRoute) {
            line.visible(false);
            bikePathPolyLineInRoute.add(mMap.addPolyline(line));
        }
        isBikePathPolylinesAdded = true;
    }

    public double getAlgorithmScore() {
        return this.algorithmScore;
    }

    public void setAlgorithmScore(double algorithmScore) {
        this.algorithmScore = algorithmScore;
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

    public void hideBikePathFromMap() {
        if (!isBikePathPolylinesAdded) {
            return;
        }
        for (Polyline line : bikePathPolyLineInRoute) {
            line.setVisible(false);
        }
        isBikePathShown = false;
    }

    public void removeBikePathFromMap() {
        if (!isBikePathPolylinesAdded) {
            return;
        }
        for (Polyline line : bikePathPolyLineInRoute) {
            line.remove();
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
            line.width(13);
        }
        return line;
    }

    public boolean isBikePathShown (){
        return isBikePathShown;
    }

    public List<LatLng> getRouteLatLngs(){
        return directionsRoute.overviewPolyline.decodePath();
    }

    private long calculateEstimatedBikingDuration( ) {
        long walkingDuration = 0;
        for (DirectionsLeg leg : directionsRoute.legs) {
            walkingDuration += leg.duration.inSeconds;
        }
        duration = Math.round(walkingDuration *  WALKING_SPEED / BIKIG_SPEED) ;
        return duration; // in seconds
    }

    private String updateDurationString( ) {
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;

        durationString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return durationString;
    }

    private String updateDistanceString() {
        if (distance < 1000){
            distanceString = String.format("%d m", distance);
        }
        else{
            distanceString = String.format("%.2f km", (double) distance/1000);
        }
        return distanceString;
    }
    
    public long getDuration() {
        return duration;
    }

    public String getDurationString() {
        return durationString;
    }

    public long getDistance() {
        return distance;
    }

    public String getDistanceString() {
        return distanceString;
    }


    public double getAverageUphillDegree() {
        Log.d("DEBUG",String.format("Worst uphill degree: %f", worstDegree));
        Log.d("DEBUG",String.format("Algorithm score: %f", pathElevationScore));
        return averageUphillDegree;
    }

    public double getBikePathPercentage() {
        return bikePathPercentage;
    }

    public double getWorstDegree() {
        return worstDegree;
    }

    public double getPathElevationScore() {
        return pathElevationScore;
    }

    public double[] getDegreesArray(){
        return pathElevationScoreCalculator.getDegreesArray();
    }

    public ElevationResult[] getRouteElevationArr() {
        return routeElevationArr;
    }
}
