package com.nnys.bikeable;

import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
//import com.google.maps.android.geometry;



import java.util.ArrayList;

/**
 * Created by Yishay on 12/10/2015.
 */
public class BikePathCalculator {

    private PolylineOptions routePolylineOpt;
    private LatLngBounds routeBounds;
    private DirectionsRoute directionsRoute;
    private ArrayList <Polyline> iriaPathsList;
    private ArrayList <Polyline> resultPathsList;
    private ArrayList <Integer> inBoundPathsNums;
    private ArrayList <Integer> inRoutePathsNums;
    private GoogleMap bikeMap;
    private double northest, southest, eastest, westest;

    public BikePathCalculator (PolylineOptions currentRoutePolylineOpt, LatLngBounds currentRouteBounds,
                               ArrayList <Polyline> iriaPaths, GoogleMap mMap, DirectionsRoute currDirectionRoute){
        routePolylineOpt = currentRoutePolylineOpt;
        routeBounds = currentRouteBounds;
        iriaPathsList = iriaPaths;
        directionsRoute = currDirectionRoute;
        bikeMap = mMap;
        resultPathsList = new ArrayList<>();
        inBoundPathsNums = new ArrayList<Integer>();
        inRoutePathsNums = new ArrayList<Integer>();
        manualCalcBounds();
        getPathsNumberInBounds();
        getPathsInRoute();
        drawBikePathInRoute();
    }

    public void getPathsInRoute (){
        for (Integer pathNum : inBoundPathsNums){
            if (isPathIn(iriaPathsList.get(pathNum))){
                inRoutePathsNums.add(pathNum);
            }
        }
    }

    public float getBikePathPercentage (long routeDistance, float bikePathDistance){
        return bikePathDistance/(float)routeDistance;
    }

    public float getTotalBikePathDitance (){
        float distance = 0;
        ArrayList <LatLng> pathPoints;
        for (Integer pathNum : inRoutePathsNums){
            pathPoints = (ArrayList)iriaPathsList.get(pathNum).getPoints();
            for (int i = 0; i < pathPoints.size() - 1; i++){
                distance += getDistanceBetweenTwoPoints(pathPoints.get(i), pathPoints.get(i+1));
            }
        }
        return distance;
    }

    public float getDistanceBetweenTwoPoints (LatLng p1, LatLng p2){
        float[] results = {0};
            Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results);
        return results[0];
    }

    public long getCurrRouteDistance () {
        long distance = 0;
        for (DirectionsLeg leg : directionsRoute.legs) {
            distance += leg.distance.inMeters;
        }
        return distance;
    }

    public boolean isPathIn (Polyline bikePath){
        boolean firstPointIn = false;
        boolean lastPointIn = false;
        boolean midPointIn = false;
        ArrayList <LatLng> bikePathPoints = (ArrayList) bikePath.getPoints();
        firstPointIn = checkPoint(bikePathPoints.get(0));
        lastPointIn = checkPoint(bikePathPoints.get(bikePathPoints.size() - 1));
        midPointIn = checkPoint(bikePathPoints.get((bikePathPoints.size())/2));


        if (firstPointIn && lastPointIn &&midPointIn){
            return true;
        }

        return false;
    }

    public boolean checkPoint (LatLng point){
        /*ArrayList <LatLng> points = (ArrayList) routePolylineOpt.getPoints();
        double latDif = 0;
        double logDif = 0;
        for (LatLng routePoint : points){
            if ((point.latitude - routePoint.latitude) < 0.002 && (point.longitude - routePoint.longitude) < 0.002){
                return true;
            }
            /*if (point.latitude < (routePoint.latitude + 0.002) && point.latitude > (routePoint.latitude - 0.002) &&
                    point.longitude < (routePoint.longitude + 0.002) && point.longitude > (routePoint.longitude - 0.002)){
                return true;
            }
        }*/
        if (PolyUtil.isLocationOnPath(point, routePolylineOpt.getPoints(), true, 15)){
            return true;
        }

        return false;
    }

    public boolean isPointInBounds (LatLng point){
        return routeBounds.contains(point);
    }

    public void manualCalcBounds (){
        double currNorth, currSouth, currEast, currWest;
        ArrayList<LatLng> points = (ArrayList) routePolylineOpt.getPoints();
        LatLng first = points.get(0);
        northest = first.latitude; southest = first.latitude;
        eastest = first.longitude; westest = first.longitude;

        for (int i = 0; i < points.size(); i++){
            currNorth = currSouth = points.get(i).latitude;
            currEast = currWest = points.get(i).longitude;
            if (northest < currNorth){
                northest = currNorth;
            }
            if (southest > currSouth){
                southest = currSouth;
            }
            if (westest > currWest){
                westest= currWest;
            }
            if (eastest < currEast){
                eastest = currEast;
            }
        }
    }

    public void getPathsNumberInBounds(){
        int j = 0;
        for (Polyline path : iriaPathsList){
            ArrayList<LatLng> points = (ArrayList)path.getPoints();
            LatLng point = points.get(0);
            if (manualIsPointInBounds(point)){
                inBoundPathsNums.add(j);
            }
            j++;
        }
    }

    public boolean manualIsPointInBounds (LatLng point){
        if (point.latitude < northest && point.latitude > southest &&
                point.longitude < eastest && point.longitude > westest){
            return true;
        }
        return false;
    }

    public void drawBikePathInRoute(){
        PolylineOptions bikePathPolyLineOpt;
        for (Integer bikePathNum : inRoutePathsNums){
            bikePathPolyLineOpt = new PolylineOptions();
            ArrayList <LatLng> bikePathPoints = (ArrayList) iriaPathsList.get(bikePathNum).getPoints();
            for (LatLng point : bikePathPoints){
                bikePathPolyLineOpt.add(point);
            }
            bikePathPolyLineOpt.color(Color.MAGENTA);
            bikeMap.addPolyline(bikePathPolyLineOpt);
        }
    }


    public ArrayList<Polyline> getResultPathsList() {
        return resultPathsList;
    }
    public void setResultPathsList(ArrayList<Polyline> result) {
        resultPathsList = result;
    }
}
