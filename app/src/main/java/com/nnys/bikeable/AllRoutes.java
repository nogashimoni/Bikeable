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

import java.util.ArrayList;

public class AllRoutes {

    ArrayList<BikeableRoute> bikeableRoutes;
    private int selectedRouteIndex;
    private boolean isTelOFunMarkersAdded;
    private boolean isTelOFunSourceStationsShown;
    private boolean isTelOFunDestinantionStationsShown;
    ArrayList <Marker> telOFunSourceMarkers;
    ArrayList <Marker> telOFunDestinationMarkers;

    public AllRoutes() {
        bikeableRoutes = new ArrayList<>();
        selectedRouteIndex  = -1;
    }

    public void updateBikeableRoutesAndMap(DirectionsRoute[] directionsRouteArr, GoogleMap mMap) {
        removeCurrentRoutes();
        addNewRoutes(directionsRouteArr, mMap);
    }

    private void addNewRoutes(DirectionsRoute[] directionsRouteArr, GoogleMap mMap) {
        for (DirectionsRoute directionsRoute: directionsRouteArr){
            bikeableRoutes.add(new BikeableRoute(directionsRoute, mMap));
        }
    }

    private void removeCurrentRoutes() {
        selectedRouteIndex = -1;
        removePolylinesFromMap();
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
                bikeableRoutes.get(i).routePolyline.setColor(Color.BLACK);
                bikeableRoutes.get(i).routePolyline.setZIndex(0);
            }
        }
    }

    public BikeableRoute getSelectedRoute (){
        if (selectedRouteIndex < 0){
            return null;
        }
        return bikeableRoutes.get(selectedRouteIndex);
    }

    public void findTelOFunMatchesToSourceAndDestination (GoogleMap mMap, DirectionsManager directionsManager){
        ArrayList <com.google.android.gms.maps.model.LatLng> closestTelOFunStationsSource =
                findClosestTelOFunStations(directionsManager.getFromLatLng());
        ArrayList <com.google.android.gms.maps.model.LatLng> closestTelOFunStationsDestination =
                findClosestTelOFunStations(directionsManager.getToLatLng());
        telOFunSourceMarkers = addClosestTelOFunToMap(mMap, closestTelOFunStationsSource);
        telOFunDestinationMarkers = addClosestTelOFunToMap(mMap, closestTelOFunStationsDestination);
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
