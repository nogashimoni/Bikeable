package com.nnys.bikeable;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.TravelMode;

public class DirectionsManager {

    private GeoApiContext context;
    private GoogleMap mMap;
    private DirectionsRoute[] calculatedRoutes;
    private LatLngBounds.Builder directionBoundsBuilder;
    private LatLngBounds directionBounds;
    private com.google.android.gms.maps.model.LatLng fromLatLng, toLatLng;
    private String fromTitle, toTitle;

    private Marker fromMarkerCurr, fromMarkerNew, toMarkerCurr, toMarkerNew;

//    private AutocompletePrediction from;
//    private AutocompletePrediction to;
//    private PlaceDetails from_placeDetails, to_placeDetails;


    public DirectionsManager(GeoApiContext context, GoogleMap mMap){
        this.context = context;
        if (mMap != null)
            this.mMap = mMap;
        calculatedRoutes = null;
        directionBounds = null;
    }

//    public void getDirections(AutocompletePrediction from, AutocompletePrediction to){
//        try {
//            PlaceDetails from_placeDetails = PlacesApi.placeDetails(context, from.getPlaceId()).await();
//            PlaceDetails to_placeDetails = PlacesApi.placeDetails(context, to.getPlaceId()).await();
//            fromLatLng = MapUtils.getGmsLatLngFromModel(from_placeDetails.geometry.location);
//            toLatLng = MapUtils.getGmsLatLngFromModel(to_placeDetails.geometry.location);
//            calculatedRoutes = DirectionsApi.newRequest(context)
//                    .alternatives(true)
//                    .mode(TravelMode.WALKING)
//                    .origin(from_placeDetails.geometry.location)
//                    .destination(to_placeDetails.geometry.location)
//                    .await();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        fromTitle = from.getDescription();
//        toTitle = to.getDescription();
//
//        clearMarkersFromMap();
//
//        updateBounds();
//        drawRouteMarkers(false, false);
//    }
//
//
//    public void getDirections(AutocompletePrediction from, com.google.android.gms.maps.model.LatLng toLatLng){
//        try {
//            PlaceDetails from_placeDetails = PlacesApi.placeDetails(context, from.getPlaceId()).await();
//            fromLatLng = MapUtils.getGmsLatLngFromModel(from_placeDetails.geometry.location);
//            this.toLatLng = toLatLng;
//            calculatedRoutes = DirectionsApi.newRequest(context)
//                    .alternatives(true)
//                    .mode(TravelMode.WALKING)
//                    .origin(from_placeDetails.geometry.location)
//                    .destination(MapUtils.getModelLatLngFromGms(toLatLng))
//                    .await();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        fromTitle = from.getDescription();
//        toTitle = "Custom destination";
//
//        clearMarkersFromMap();
//        updateBounds();
//        drawRouteMarkers(false, true);
//    }
//
//
//    public void getDirections(com.google.android.gms.maps.model.LatLng fromLatLng, AutocompletePrediction to, boolean isFromCurr){
//        try {
//            this.fromLatLng = fromLatLng;
//            PlaceDetails to_placeDetails = PlacesApi.placeDetails(context, to.getPlaceId()).await();
//            toLatLng = MapUtils.getGmsLatLngFromModel(to_placeDetails.geometry.location);
//            calculatedRoutes = DirectionsApi.newRequest(context)
//                    .alternatives(true)
//                    .mode(TravelMode.WALKING)
//                    .origin(MapUtils.getModelLatLngFromGms(fromLatLng))
//                    .destination(to_placeDetails.geometry.location)
//                    .await();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        toTitle = to.getDescription();
//        fromTitle = isFromCurr ? "Current Location" : "Custom Origin";
//
//        drawRouteMarkers(isFromCurr, false);
//        clearMarkersFromMap();
//        updateBounds();
//    }

    public void getDirections(com.google.android.gms.maps.model.LatLng fromLatLng,
                              com.google.android.gms.maps.model.LatLng toLatLng,
                              String fromTitle,
                              String toTitle){

        this.fromLatLng = fromLatLng;
        this.toLatLng = toLatLng;

        this.fromTitle = fromTitle; //isFromCurr ? "Current Location" : "Custom Origin";
        this.toTitle =  toTitle; //"Custom destination";

        try {
            calculatedRoutes = DirectionsApi.newRequest(context)
                    .alternatives(true)
                    .mode(TravelMode.WALKING)
                    .origin(MapUtils.getModelLatLngFromGms(fromLatLng))
                    .destination(MapUtils.getModelLatLngFromGms(toLatLng))
                    .await();

        } catch (Exception e) {
            e.printStackTrace();
        }

        clearMarkersFromMap();
        drawRouteMarkers();
        updateBounds();
    }

    protected void drawRouteMarkers(){
        if (fromMarkerCurr != null){
            fromMarkerCurr.remove();
        }
        if (toMarkerCurr != null){
            toMarkerCurr.remove();
        }

        fromMarkerCurr = mMap.addMarker(
                    new MarkerOptions()
                            .title(fromTitle)
                            .position(fromLatLng)
        );

        toMarkerCurr = mMap.addMarker(
                    new MarkerOptions()
                        .title(toTitle)
                        .position(toLatLng)
        );
     }

    protected void clearMarkersFromMap() {
        if (fromMarkerNew != null)
            fromMarkerNew.remove();
        if (toMarkerNew != null)
            toMarkerNew.remove();
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

    public Marker getFromMarkerCurr() {
        return fromMarkerCurr;
    }

    public Marker getToMarkerCurr() {
        return toMarkerCurr;
    }

    public void setToMarkerNew(Marker toMarkerNew) {
        this.toMarkerNew = toMarkerNew;
    }

    public void setFromMarkerNew(Marker fromMarkerNew) {
        this.fromMarkerNew = fromMarkerNew;
    }

    public Marker getFromMarkerNew() {
        return fromMarkerNew;
    }

    public Marker getToMarkerNew() {
        return toMarkerNew;
    }

    public void setMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    private void updateBounds(){
        directionBoundsBuilder = new LatLngBounds.Builder();
        directionBoundsBuilder.include(fromLatLng);
        directionBoundsBuilder.include(toLatLng);
        directionBounds = directionBoundsBuilder.build();
    }

    public void setNewMarkerByPlacePrediction(boolean isFrom, AutocompletePrediction prediction) {

        PlaceDetails placeDetails = null;
        try {
            placeDetails = PlacesApi.placeDetails(context, prediction.getPlaceId()).await();
        } catch (Exception e) {
            e.printStackTrace(); // todo: handle this properly
        }
        LatLng markerLatLng = MapUtils.getGmsLatLngFromModel(placeDetails.geometry.location);
        String markerTitle = prediction.getDescription();
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(markerLatLng).title(markerTitle));
        if (isFrom){
            if (this.getFromMarkerNew() != null){
                this.getFromMarkerNew().remove();
            }
            if (this.fromMarkerCurr != null){
                this.fromMarkerCurr.remove();
            }
            this.setFromMarkerNew(newMarker);
        }
        else{ // isTo
            if (this.getToMarkerNew() != null){
                this.getToMarkerNew().remove();
            }
            if (this.toMarkerCurr != null){
                this.toMarkerCurr.remove();
            }
            this.setToMarkerNew(newMarker);
        }
    }

    public void setNewMarkerByCustomPrediction(boolean isFrom, LatLng markerLatLng, CustomAutoCompletePrediction prediction) {

        Marker newMarker = mMap.addMarker(new MarkerOptions().position(markerLatLng));
        if (isFrom){
            if (this.fromMarkerNew != null){
                this.fromMarkerNew.remove();
            }
            if (this.fromMarkerCurr != null){
                this.fromMarkerCurr.remove();
            }
            newMarker.setTitle(prediction.getDescription());
            this.setFromMarkerNew(newMarker);
        }
        else {  // isTo
            if (this.toMarkerNew != null){
                this.toMarkerNew.remove();
            }
            if (this.toMarkerCurr != null){
                this.toMarkerCurr.remove();
            }
            newMarker.setTitle("Custom destination");
            this.setToMarkerNew(newMarker);
        }

    }

    public void clearNewMarker(boolean isFrom) {
        if (isFrom && fromMarkerNew != null){
            fromMarkerNew.remove();
            fromMarkerNew = null;
        }
        else if (!isFrom && toMarkerNew != null){
            toMarkerNew.remove();
            toMarkerNew = null;
        }
    }
}
