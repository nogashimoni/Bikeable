package com.nnys.bikeable;

import android.renderscript.Sampler;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

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
    double bikePathPercentage;

    boolean isBikePathPolylinesAdded;
    boolean isBikePathShown;
    boolean isTelOFunMarkersAdded;
    boolean isTelOFunSourceStationsShown;
    boolean isTelOFunDestinantionStationsShown;
    ArrayList <PolylineOptions> bikePathInRoute;
    ArrayList <Polyline> bikePathPolyLineInRoute;

    ArrayList <com.google.android.gms.maps.model.LatLng> closestTelOFunStationsSource;
    ArrayList <com.google.android.gms.maps.model.LatLng> closestTelOFunStationsDestination;
    ArrayList<Marker> telOFunSourceMarkers;
    ArrayList<Marker> telOFunDestinationMarkers;

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
            ArrayList <com.google.android.gms.maps.model.LatLng> routePoints = (ArrayList) routePolylineOptions.getPoints();
            closestTelOFunStationsSource = findClosestTelOFunStations(routePoints.get(0));
            closestTelOFunStationsDestination = findClosestTelOFunStations(routePoints.get(routePoints.size() - 1));
            telOFunSourceMarkers = addClosestTelOFunToMap(mMap, closestTelOFunStationsSource);
            telOFunDestinationMarkers = addClosestTelOFunToMap(mMap, closestTelOFunStationsDestination);
        }
    }

    public void addBikePathToMap (GoogleMap mMap) {
        if (isBikePathPolylinesAdded)
            return;
        bikePathPolyLineInRoute = new ArrayList<>();
        for (PolylineOptions line : bikePathInRoute) {
            line.visible(false);
            bikePathPolyLineInRoute.add(mMap.addPolyline(line));
        }
        isBikePathPolylinesAdded = true;
    }

    public ArrayList <Marker> addClosestTelOFunToMap (GoogleMap mMap, ArrayList <com.google.android.gms.maps.model.LatLng> stations) {
        ArrayList <Marker> telOFunMarkers = new ArrayList<>();
        for (com.google.android.gms.maps.model.LatLng station : stations) {
            telOFunMarkers.add(
                    mMap.addMarker(new MarkerOptions()
                            .position(station)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .visible(false)));
        }
        isTelOFunMarkersAdded = true;
        return telOFunMarkers;
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

    public void showSourceTelOFunOnMap() {
        if (!isTelOFunMarkersAdded){
            return;
        }
        for (Marker station : telOFunSourceMarkers){
            station.setVisible(true);
        }
        isTelOFunSourceStationsShown = true;
    }

    public void removeSourceTelOFunFromMap() {
        if (!isTelOFunMarkersAdded) {
            return;
        }
        for (Marker station : telOFunSourceMarkers) {
            station.setVisible(false);
        }
        isTelOFunSourceStationsShown = false;
    }

    public void showDestinationTelOFunOnMap() {
        if (!isTelOFunMarkersAdded){
            return;
        }
        for (Marker station : telOFunDestinationMarkers){
            station.setVisible(true);
        }
        isTelOFunDestinantionStationsShown = true;
    }

    public void removeDestinationTelOFunFromMap() {
        if (!isTelOFunMarkersAdded) {
            return;
        }
        for (Marker station : telOFunDestinationMarkers) {
            station.setVisible(false);
        }
        isTelOFunDestinantionStationsShown = false;
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

    private ArrayList<com.google.android.gms.maps.model.LatLng> findClosestTelOFunStations (com.google.android.gms.maps.model.LatLng point){
        ArrayList <com.google.android.gms.maps.model.LatLng> closestStations = new ArrayList<>();
        ArrayList <com.google.android.gms.maps.model.LatLng> allStations = IriaData.getTelOfanStationsList();
        PolylineOptions onePointPolyLine = new PolylineOptions();
        onePointPolyLine.add(point);
        for (com.google.android.gms.maps.model.LatLng station : allStations){
            if (PolyUtil.isLocationOnPath(station, onePointPolyLine.getPoints(), true, 200)){
                closestStations.add(station);
            }
        }
        return closestStations;
    }

    public boolean isBikePathShown (){
        return isBikePathShown;
    }

    public List<LatLng> getRouteLatLngs(){
        return directionsRoute.overviewPolyline.decodePath();
    }

}
