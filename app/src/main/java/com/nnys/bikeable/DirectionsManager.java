package com.nnys.bikeable;

import android.graphics.Color;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;

/**
 * Created by Yishay on 11/21/2015.
 */
public class DirectionsManager {

    private GeoApiContext context;
    private DirectionsRoute[] routes;
    private int numRoutes;
    private AutocompletePrediction from;
    private AutocompletePrediction to;
    private com.google.android.gms.maps.model.LatLng fromLatLng, toLatLng;
    private PlaceDetails from_placeDetails, to_placeDetails;
    private LatLngBounds.Builder directionBoundsBuilder;
    private LatLngBounds directionBounds;
    private ArrayList<PolylineOptions> routesPolylineOpts;
    private ArrayList<Polyline> routesPolylines;
    private int selectedRouteIndex;

    public DirectionsManager(GeoApiContext context, AutocompletePrediction from, AutocompletePrediction to){
        this.from = from;
        this.to = to;
        try { //TODO: handle failure here
            from_placeDetails = PlacesApi.placeDetails(context, from.getPlaceId()).await();
            to_placeDetails = PlacesApi.placeDetails(context, to.getPlaceId()).await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fromLatLng = MapUtils.getGmsLatLngFromModel(from_placeDetails.geometry.location);
        toLatLng = MapUtils.getGmsLatLngFromModel(to_placeDetails.geometry.location);
        try {
            routes = DirectionsApi.newRequest(context)
                    .alternatives(true)
                    .mode(TravelMode.WALKING)
                    .origin(from_placeDetails.geometry.location)
                    .destination(to_placeDetails.geometry.location)
                    .await();
//                    getDirections(context,
//                    from.getDescription(), to.getDescription()).alternatives(false).await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        numRoutes = routes.length;
        createRoutesPolyOpts();
        directionBoundsBuilder = new LatLngBounds.Builder();
        directionBoundsBuilder.include(fromLatLng);
        directionBoundsBuilder.include(toLatLng);
        directionBounds = directionBoundsBuilder.build();
        selectedRouteIndex = -1;
    }

    protected void createRoutesPolyOpts(){
        routesPolylineOpts = new ArrayList<>();
        PolylineOptions line;
        com.google.android.gms.maps.model.LatLng currPoint;
        for (DirectionsRoute route : routes) {
            line = new PolylineOptions();
            for (LatLng point : route.overviewPolyline.decodePath()) {
                currPoint = new com.google.android.gms.maps.model.LatLng(point.lat, point.lng);
                line.add(currPoint);
            }
            routesPolylineOpts.add(line);
        }
    }

    protected void drawAllRoutes(GoogleMap mMap){
        routesPolylines = new ArrayList<>();
        for (PolylineOptions line : routesPolylineOpts){
            routesPolylines.add(mMap.addPolyline(line));
        }
    }

    protected void drawRouteMarkers(GoogleMap mMap){
        MarkerOptions fromMarker = new MarkerOptions()
                .title(from.getDescription())
                .position(fromLatLng);
        MarkerOptions toMarker = new MarkerOptions()
                .title(to.getDescription())
                .position(toLatLng);
        mMap.addMarker(fromMarker);
        mMap.addMarker(toMarker);

    }

    protected void selectAndColorRoute (int routeInd){
        for (int i = 0; i < numRoutes; i++){
            if (i == routeInd){
                routesPolylines.get(i).setColor(Color.BLUE);
                routesPolylines.get(i).setZIndex(1);
            }
            else{
                routesPolylines.get(i).setColor(Color.BLACK);
                routesPolylines.get(i).setZIndex(0);
            }
        }
    }

    public DirectionsRoute getCurrentRout () {
        return routes[selectedRouteIndex];
    }

    public EncodedPolyline getCurrRouteEncPolyline() {
        return getCurrentRout().overviewPolyline;
    }

    public long getCurrRouteDistance () {
        long distance = 0;
        for (DirectionsLeg leg : getCurrentRout().legs) {
            distance += leg.distance.inMeters;
        }
        return distance;
    }
    public DirectionsRoute[] getRoutes (){
        return routes;
    }

    public LatLngBounds getDirectionBounds(){
        return directionBounds;
    }

    public ArrayList<PolylineOptions> getRoutesPolylineOpts (){
        return routesPolylineOpts;
    }

    public int getNumRoutes (){
        return numRoutes;
    }

    public int getSelectedRouteIndex(){
        return selectedRouteIndex;
    }

    public ArrayList<Polyline> getRoutesPolylines (){
        return routesPolylines;
    }


    public void setSelectedRouteIndex(int routeNum){
        selectedRouteIndex = routeNum;
    }
}
