package com.nnys.bikeable;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AllRoutes {

    ArrayList<BikeableRoute> bikeableRoutes;
    private int selectedRouteIndex;
    private int bestRouteIndex;
    private boolean isTelOFunMarkersAdded;
    private boolean isTelOFunSourceStationsShown;
    private boolean isTelOFunDestinantionStationsShown;
    ArrayList <TelOFunStation> closestTelOFunStationsSource;
    ArrayList <TelOFunStation> closestTelOFunStationsDestination;
    ArrayList <Marker> telOFunSourceMarkers;
    ArrayList <Marker> telOFunDestinationMarkers;

    public AllRoutes() {
        bikeableRoutes = new ArrayList<>();
        selectedRouteIndex  = -1;
    }

    public void updateBikeableRoutesAndMap(DirectionsRoute[] directionsRouteArr, GoogleMap mMap) {
        removeCurrentRoutes();
        addNewRoutes(directionsRouteArr, mMap);
        bestRouteIndex = calculateBestRouteIndex(); // by now, all routes are already updated

        // mark the best route at the beginning
        bikeableRoutes.get(bestRouteIndex).routePolyline.setColor(0xFF11b468); // color best route in orange (like golden route)
        bikeableRoutes.get(bestRouteIndex).routePolyline.setZIndex(1); // be on top of other routes
    }

    private void addNewRoutes(DirectionsRoute[] directionsRouteArr, GoogleMap mMap) {
        for (DirectionsRoute directionsRoute: directionsRouteArr){
            bikeableRoutes.add(new BikeableRoute(directionsRoute, mMap));
        }
    }

    private void removeCurrentRoutes() {
        selectedRouteIndex = -1;
        removePolylinesFromMap();
        removeTelOFunMatchesFromMap();
        bikeableRoutes.removeAll(bikeableRoutes);
    }

    private void removePolylinesFromMap() {
        for (BikeableRoute bikeableRoute : bikeableRoutes){
            bikeableRoute.routePolyline.remove();
            bikeableRoute.removeBikePathFromMap();
        }
    }

    protected void selectAndColorRoute (int routeInd){
        for (int i = 0; i < getNumRoutes(); i++){
            if (i == routeInd){
                bikeableRoutes.get(i).routePolyline.setColor(0xFF84E0FF);
                bikeableRoutes.get(i).routePolyline.setZIndex(1);
            }
            else{
                if (i == bestRouteIndex)
                    bikeableRoutes.get(i).routePolyline.setColor(0xFF11b468);
                else
                    bikeableRoutes.get(i).routePolyline.setColor(Color.BLACK);
                bikeableRoutes.get(i).routePolyline.setZIndex(0);
            }
        }
    }



    private int calculateBestRouteIndex() {
        // This is a naive implemintation. We need to find a better way to calculate.
        double maxScore = -1000000; //min int
        int bestRouteIndex = 0;
        for (int i=0; i<getNumRoutes(); i++) {
            double maxElevationScorePerSearch = getMaxElevationScorePerSearch();
            double rescaledElevationScore = calcRescaledElevationScore(bikeableRoutes.get(i), maxElevationScorePerSearch);
            double rescaledBikePathScore = calcRescaledBikePathsScore(bikeableRoutes.get(i));
            double pathFinalScore = rescaledElevationScore + rescaledBikePathScore;
            Log.i("INFO", String.format("Final Score: route with index %d has final score of %f", i , pathFinalScore));
            if (pathFinalScore > maxScore) {
                maxScore = pathFinalScore;
                bestRouteIndex = i;
            }
        }
        return bestRouteIndex;
    }

    private double getMaxElevationScorePerSearch() {
        double maxElevationScorePerSearch = 0;
        for ( BikeableRoute route : bikeableRoutes ) {
            double currMax = route.getPathElevationScore();
            if ( currMax > maxElevationScorePerSearch ) {
                maxElevationScorePerSearch = currMax;
            }
        }
        return maxElevationScorePerSearch;
    }

    private double calcRescaledElevationScore(BikeableRoute route, double maxElevationScorePerSearch) {
        double ratio = route.getPathElevationScore() / maxElevationScorePerSearch ;
        return -1 * ratio;  // the bigger the result is, the more easy the route should be, therefore we multiply by -1
    }

    private double calcRescaledBikePathsScore(BikeableRoute route) {
        return route.getBikePathPercentage();
    }

    public BikeableRoute getSelectedRoute (){
        if (selectedRouteIndex < 0){
            return null;
        }
        return bikeableRoutes.get(selectedRouteIndex);
    }

    public void calculateClosestTelOFunStationsData (GoogleMap mMap, DirectionsManager directionsManager) throws IOException {
        chooseTelOFunMatchesToSourceAndDestination (mMap, directionsManager);
        IriaData.updateTelOFunBikesAvailability();
    }

    public void chooseTelOFunMatchesToSourceAndDestination (GoogleMap mMap, DirectionsManager directionsManager){
        closestTelOFunStationsSource = findClosestTelOFunStations(directionsManager.getFromLatLng());
        closestTelOFunStationsDestination = findClosestTelOFunStations(directionsManager.getToLatLng());
        telOFunSourceMarkers = addClosestTelOFunToMap(mMap, closestTelOFunStationsSource);
        telOFunDestinationMarkers = addClosestTelOFunToMap(mMap, closestTelOFunStationsDestination);
    }

    public ArrayList <Marker> addClosestTelOFunToMap (GoogleMap mMap, ArrayList <TelOFunStation> stations) {
        ArrayList <Marker> telOFunMarkers = new ArrayList<>();
        for (TelOFunStation station : stations) {
            telOFunMarkers.add(
                    mMap.addMarker(new MarkerOptions()
                            .title("TelOFun").snippet(Integer.toString(station.getId()))
                            .position(station.getCoordinates())
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .visible(false)));
        }
        isTelOFunMarkersAdded = true;
        return telOFunMarkers;
    }

    private ArrayList<TelOFunStation> findClosestTelOFunStations (com.google.android.gms.maps.model.LatLng point){
        ArrayList <TelOFunStation> closestStations = new ArrayList<>();
        HashMap <Integer, TelOFunStation> allStations = IriaData.getTelOfanStationsDict();
        PolylineOptions onePointPolyLine = new PolylineOptions();
        onePointPolyLine.add(point);
        for (TelOFunStation station: allStations.values()){
            if (PolyUtil.isLocationOnPath(station.getCoordinates(), onePointPolyLine.getPoints(), true, 200)){
                closestStations.add(station);
            }
        }
        return closestStations;
    }

    public void showTelOFunSourceMatchesOnMap() {
        if (!isTelOFunMarkersAdded){
            return;
        }
        for (Marker station : telOFunSourceMarkers){
            station.setVisible(true);
        }
        isTelOFunSourceStationsShown = true;
    }

    public void hideTelOFunSourceMatchesOnMap() {
        if (!isTelOFunMarkersAdded) {
            return;
        }
        for (Marker station : telOFunSourceMarkers) {
            station.setVisible(false);
        }
        isTelOFunSourceStationsShown = false;
    }

    public void showTelOFunDestinationMatchesOnMap() {
        if (!isTelOFunMarkersAdded){
            return;
        }
        for (Marker station : telOFunDestinationMarkers){
            station.setVisible(true);
        }
        isTelOFunDestinantionStationsShown = true;
    }

    public void hideTelOFunDestinationMatchesOnMap() {
        if (!isTelOFunMarkersAdded) {
            return;
        }
        for (Marker station : telOFunDestinationMarkers) {
            station.setVisible(false);
        }
        isTelOFunDestinantionStationsShown = false;
    }

    public void removeTelOFunMatchesFromMap() {
        if (!isTelOFunMarkersAdded) {
            return;
        }
        for (Marker station : telOFunSourceMarkers) {
            station.remove();
        }
        for (Marker station : telOFunDestinationMarkers) {
            station.remove();
        }
        isTelOFunSourceStationsShown = false;
        isTelOFunMarkersAdded = false;
    }


    public int getNumRoutes(){
        return bikeableRoutes.size();
    }

    public ArrayList<BikeableRoute> getAllRoutes(){
        return bikeableRoutes;
    }

    public int getSelectedRouteIndex() {
        return selectedRouteIndex;
    }

    public void setSelectedRouteIndex(int selectedRouteIndex) {
        this.selectedRouteIndex = selectedRouteIndex;
    }

    public BikeableRoute getRouteByIndex(int index){
        return bikeableRoutes.get(index);
    }
}
