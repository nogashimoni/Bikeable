package com.nnys.bikeable;

import android.text.style.CharacterStyle;
import android.util.Log;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

public class DirectionsManager {

    private GeoApiContext context;
    private DirectionsRoute[] calculatedRoutes;
    private AutocompletePrediction from;
    private AutocompletePrediction to;
    private com.google.android.gms.maps.model.LatLng fromLatLng, toLatLng;
    private PlaceDetails from_placeDetails, to_placeDetails;
    private LatLngBounds.Builder directionBoundsBuilder;
    private LatLngBounds directionBounds;
    private ArrayList<Marker> directionMarkers;

    public DirectionsManager(GeoApiContext context, AutocompletePrediction from, AutocompletePrediction to){
        this.from = from;
        this.to = to;
        try { //TODO: handle failure here properly
            from_placeDetails = PlacesApi.placeDetails(context, from.getPlaceId()).await();
            to_placeDetails = PlacesApi.placeDetails(context, to.getPlaceId()).await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fromLatLng = MapUtils.getGmsLatLngFromModel(from_placeDetails.geometry.location);
        toLatLng = MapUtils.getGmsLatLngFromModel(to_placeDetails.geometry.location);
        try {
            calculatedRoutes = DirectionsApi.newRequest(context)
                    .alternatives(true)
                    .mode(TravelMode.WALKING)
                    .origin(from_placeDetails.geometry.location)
                    .destination(to_placeDetails.geometry.location)
                    .await();

        } catch (Exception e) {
            e.printStackTrace();
        }
        directionBoundsBuilder = new LatLngBounds.Builder();
        directionBoundsBuilder.include(fromLatLng);
        directionBoundsBuilder.include(toLatLng);
        directionBounds = directionBoundsBuilder.build();
    }

    public DirectionsManager(GeoApiContext context, com.google.android.gms.maps.model.LatLng cuurentLocationLatLng, AutocompletePrediction to) {
        this.from = null;
        this.to = to;
        try { //TODO: handle failure here properly
            from_placeDetails = null;
            to_placeDetails = PlacesApi.placeDetails(context, to.getPlaceId()).await();
            if ( to_placeDetails == null ) {
                Log.i("INFO", "to place detailes is null!!!!!!!");
                throw new Exception();
            }
            Log.i("INFO", "finished annoying part");
        } catch (Exception e) {
            e.printStackTrace();
        }
        fromLatLng = new com.google.android.gms.maps.model.LatLng(cuurentLocationLatLng.latitude, cuurentLocationLatLng.longitude);

        toLatLng = MapUtils.getGmsLatLngFromModel(to_placeDetails.geometry.location);
        try {
            calculatedRoutes = DirectionsApi.newRequest(context)
                    .alternatives(true)
                    .mode(TravelMode.WALKING)
                    .origin(MapUtils.getModelLatLngFromGms(fromLatLng))
                    .destination(to_placeDetails.geometry.location)
                    .await();

        } catch (Exception e) {
            e.printStackTrace();
        }
        directionBoundsBuilder = new LatLngBounds.Builder();
        directionBoundsBuilder.include(fromLatLng);
        directionBoundsBuilder.include(toLatLng);
        directionBounds = directionBoundsBuilder.build();
    }

    protected void drawRouteMarkers(GoogleMap mMap){
        String fromTitle = ( from == null ? "Current Location": from.getDescription());
        directionMarkers = new ArrayList<>();
        MarkerOptions fromMarker = new MarkerOptions()
                .title(fromTitle)
                .position(fromLatLng);
        MarkerOptions toMarker = new MarkerOptions()
                .title(to.getDescription())
                .position(toLatLng);
        directionMarkers.add(mMap.addMarker(fromMarker));
        directionMarkers.add(mMap.addMarker(toMarker));
    }

    protected void clearMarkersFromMap() {
        for (Marker marker : directionMarkers){
            marker.remove();
        }
    }

    public DirectionsRoute[] getCalculatedRoutes(){
        return calculatedRoutes;
    }

    public LatLngBounds getDirectionBounds(){
        return directionBounds;
    }

    public LatLng getFromLatLng (){
        return fromLatLng;
    }

    public LatLng getToLatLng (){
        return toLatLng;
    }
}
