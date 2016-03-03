package com.nnys.bikeable;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
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

import javax.security.auth.login.LoginException;

/**
 * Created by Yishay on 12/10/2015.
 */
public class BikePathCalculator {

    private PolylineOptions routePolylineOpt;
    private DirectionsRoute directionsRoute;
    private ArrayList <PolylineOptions> iriaPathsList;
    private ArrayList <PolylineOptions> totalInRoutePaths;

    private double northest, southest, eastest, westest;
    private ArrayList<PolylineOptions> relevantIriaBikePath;

    public BikePathCalculator (PolylineOptions currentRoutePolylineOpt,
                               ArrayList <PolylineOptions> iriaPaths, DirectionsRoute currDirectionRoute){
        routePolylineOpt = currentRoutePolylineOpt;
        iriaPathsList = iriaPaths;
        directionsRoute = currDirectionRoute;
        totalInRoutePaths = new ArrayList<>();
    }

    public float getBikePathPercentageByRoute(){
        float bikePathDistance;
        float bikePathPercentage;
        long routeDistance = calcCurrRouteDistance();
        updateRelevantBikePathsToRoutes();
        calcOverlapByRoute();
        bikePathDistance = calcBikePathDitance();
        bikePathPercentage = calcBikePathPercentage(routeDistance, bikePathDistance);
        return bikePathPercentage;

    }



    public float calcBikePathPercentage (long routeDistance, float bikePathDistance){
        return bikePathDistance/(float)routeDistance;
    }

    public long calcCurrRouteDistance () {
        long distance = 0;
        for (DirectionsLeg leg : directionsRoute.legs) {
            distance += leg.distance.inMeters;
        }
        return distance;
    }

    public void calcOverlapByRoute (){
        boolean isInBikePath;
        int lastMatchedPath = 0;
        ArrayList<LatLng> points = (ArrayList) routePolylineOpt.getPoints();
        PolylineOptions currPolyLineOpt = new PolylineOptions();
        currPolyLineOpt.width(8);
        currPolyLineOpt.color(Color.MAGENTA);
        for (LatLng point : points) {
            isInBikePath = false;
            if (checkPointWithPath(point, relevantIriaBikePath.get(lastMatchedPath))){
                isInBikePath = true;
            }

            if (!isInBikePath) {
                for (int i = 0; i < relevantIriaBikePath.size(); i++) {
                    if (checkPointWithPath(point, relevantIriaBikePath.get(i))) {
                        isInBikePath = true;
                        lastMatchedPath = i;
                        break;
                    }
                }
            }
            if(isInBikePath){
                currPolyLineOpt.add(point);
            }
            else{
                if (currPolyLineOpt.getPoints().size() > 3){
                    totalInRoutePaths.add(currPolyLineOpt);
                }
                currPolyLineOpt = new PolylineOptions();
                currPolyLineOpt.width(5);
                currPolyLineOpt.color(Color.MAGENTA);

            }
        }
        if (currPolyLineOpt.getPoints().size() > 3){
            totalInRoutePaths.add(currPolyLineOpt);
        }
    }

    public boolean checkPointWithPath (LatLng point, PolylineOptions line){
        if (PolyUtil.isLocationOnPath(point, line.getPoints(), true, 30)){
            return true;
        }
        return false;
    }

    public float calcBikePathDitance (){
        float distance = 0;
        ArrayList <LatLng> pathPoints;
        for (PolylineOptions path : totalInRoutePaths){
            pathPoints = (ArrayList) path.getPoints();
            for (int i = 0; i < pathPoints.size() - 1; i++){
                distance += calcDistanceBetweenPoints(pathPoints.get(i), pathPoints.get(i+1));
            }
        }

        return distance;
    }

    public float calcDistanceBetweenPoints (LatLng p1, LatLng p2){
        float[] results = {0};
        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results);
        return results[0];
    }

    public ArrayList<PolylineOptions> getBikePaths (){
        return totalInRoutePaths;
    }


    public void updateRelevantBikePathsToRoutes (){
        ArrayList<LatLng> routePoints = (ArrayList)routePolylineOpt.getPoints();
        double marginToAdd = 0.001;
        northest = routePoints.get(0).latitude;
        southest = routePoints.get(0).latitude;
        westest = routePoints.get(0).longitude;
        eastest = routePoints.get(0).longitude;
        relevantIriaBikePath = new ArrayList<>();
        for (LatLng point : routePoints){
            updateEdges (point);
        }
        northest += marginToAdd;
        southest -= marginToAdd;
        eastest += marginToAdd;
        westest -= marginToAdd;

        for (PolylineOptions path : iriaPathsList){
            if (checkIfPathInSquare(path)) {
                relevantIriaBikePath.add(path);
            }
        }
    }

    public void updateEdges (LatLng point){
        double currLat = point.latitude;
        double currLng = point.longitude;
        if (currLat > northest){
            northest = currLat;
        }
        if (currLat < southest){
            southest = currLat;
        }
        if (currLng < westest){
            westest = currLng;
        }
        if (currLng > eastest){
            eastest = currLng;
        }
    }

    public boolean checkIfPathInSquare (PolylineOptions path) {
        ArrayList <LatLng> pathPoints = (ArrayList)path.getPoints();
        int numOfPointsInPath = pathPoints.size();
        if (checkIfPointInSquare (pathPoints.get(0)) || checkIfPointInSquare (pathPoints.get(numOfPointsInPath - 1)) ||
                checkIfPointInSquare(pathPoints.get(numOfPointsInPath/2))){
            return true;
        }
        return false;
    }

    public boolean checkIfPointInSquare (LatLng point){
        if (point.latitude < northest && point.latitude > southest && point.longitude < eastest && point.longitude > westest){
            return true;
        }
        return false;
    }
}
