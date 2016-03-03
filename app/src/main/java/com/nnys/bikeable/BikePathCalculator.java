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



    public void calcOverlapByRoute2 (){
        boolean isInBikePath;
        ArrayList<LatLng> points = (ArrayList) routePolylineOpt.getPoints();
        PolylineOptions currPolyLineOpt = new PolylineOptions();
        currPolyLineOpt.width(8);
        currPolyLineOpt.color(Color.MAGENTA);
        for (LatLng point : points) {
            isInBikePath = false;
            for (PolylineOptions line : iriaPathsList){
                if (checkPointWithPath(point, line)){
                    isInBikePath = true;
                    break;
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

    public void calcOverlapByRoute (){
        boolean isInBikePath;
        int lastMatchedPath = 0;
        ArrayList<LatLng> points = (ArrayList) routePolylineOpt.getPoints();
        PolylineOptions currPolyLineOpt = new PolylineOptions();
        currPolyLineOpt.width(5);
        currPolyLineOpt.color(Color.MAGENTA);
        for (LatLng point : points) {
            isInBikePath = false;
            if (checkPointWithPath(point, iriaPathsList.get(lastMatchedPath))){
                isInBikePath = true;
            }

            if (!isInBikePath) {
                for (int i = 0; i < iriaPathsList.size(); i++) {
                    if (checkPointWithPath(point, iriaPathsList.get(i))) {
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
}
