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
    private ArrayList <PolylineOptions> resultPartialPathList;
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
        resultPartialPathList = new ArrayList<>();
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

    public float calcBikePathPercentage (long routeDistance, float bikePathDistance){
        return bikePathDistance/(float)routeDistance;
    }

    public float calcTotalBikePathDitance (){
        float distance = 0;
        ArrayList <LatLng> pathPoints;
        for (Integer pathNum : inRoutePathsNums){
            pathPoints = (ArrayList)iriaPathsList.get(pathNum).getPoints();
            for (int i = 0; i < pathPoints.size() - 1; i++){
                distance += calcDistanceBetweenTwoPoints(pathPoints.get(i), pathPoints.get(i+1));
            }
        }
        for (PolylineOptions path : resultPartialPathList){
            pathPoints = (ArrayList)path.getPoints();
            for (int i = 0; i < pathPoints.size() - 1; i++){
                distance += calcDistanceBetweenTwoPoints(pathPoints.get(i), pathPoints.get(i+1));
            }
        }
        return distance;
    }

    public float calcDistanceBetweenTwoPoints (LatLng p1, LatLng p2){
        float[] results = {0};
            Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results);
        return results[0];
    }

    public long calcCurrRouteDistance () {
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
        boolean midPointIn2 = false;
        boolean midPointIn3 = false;
        ArrayList <LatLng> bikePathPoints = (ArrayList) bikePath.getPoints();
        firstPointIn = checkPoint(bikePathPoints.get(0));
        lastPointIn = checkPoint(bikePathPoints.get(bikePathPoints.size() - 1));
        midPointIn = checkPoint(bikePathPoints.get((bikePathPoints.size())/4));
        midPointIn2 = checkPoint(bikePathPoints.get(((bikePathPoints.size())/4)*2));
        midPointIn2 = checkPoint(bikePathPoints.get(((bikePathPoints.size())/4)*3));


        if (firstPointIn && lastPointIn && midPointIn){
            return true;
        }

        else if (firstPointIn || lastPointIn || midPointIn || midPointIn2 || midPointIn3){
            calcPartialPath(bikePathPoints);
        }

        return false;
    }

    public void calcPartialPath (ArrayList <LatLng> points){
        boolean foundFirstPoint = false;
        int numOfPointsIn = 0;
        PolylineOptions partialPath = new PolylineOptions();
        for (int i = 0; i < points.size(); i++){
            if (!foundFirstPoint){
                foundFirstPoint = checkPoint(points.get(i));
                if (foundFirstPoint){
                    partialPath.add(points.get(i));
                    numOfPointsIn++;
                    continue;
                }
            }
            if (foundFirstPoint){
                if (checkPoint(points.get(i))){
                    partialPath.add(points.get(i));
                    numOfPointsIn++;
                }
                else{
                    break;
                }
            }
        }
        if (numOfPointsIn > 2){
            resultPartialPathList.add(partialPath);
        }
    }

    public boolean checkPoint (LatLng point){
        if (PolyUtil.isLocationOnPath(point, routePolylineOpt.getPoints(), true, 35)){
            return true;
        }
        return false;
    }

    public boolean checkPartialPoint (LatLng point){
        if (PolyUtil.isLocationOnPath(point, routePolylineOpt.getPoints(), true, 50)){
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
            LatLng firstPoint = points.get(0);
            LatLng lastPoint = points.get(points.size() - 1);
            if (manualIsPointInBounds(firstPoint) || manualIsPointInBounds(lastPoint)){
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

        for (PolylineOptions bikePath : resultPartialPathList) {
            bikePath.color(Color.RED);
            bikeMap.addPolyline(bikePath);
        }
    }


    public ArrayList<Polyline> getResultPathsList() {
        return resultPathsList;
    }
    public void setResultPathsList(ArrayList<Polyline> result) {
        resultPathsList = result;
    }
}
