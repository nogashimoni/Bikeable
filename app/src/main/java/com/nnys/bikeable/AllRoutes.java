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

    public void calculateClosestTelOFunStationsData (GoogleMap mMap, DirectionsManager directionsManager) throws IOException {
        chooseTelOFunMatchesToSourceAndDestination (mMap, directionsManager);
        IriaData.updateTelOFunBikesAvailability();
        System.out.println("data: " + closestTelOFunStationsSource.get(0).getNumOfBikesAvailable());
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
