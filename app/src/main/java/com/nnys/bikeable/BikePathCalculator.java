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
    private ArrayList <PolylineOptions> iriaPathsListPolyLineOpt;
    private ArrayList <PolylineOptions> resultPartialPathList;
    private ArrayList <PolylineOptions> totalInRoutePaths;
    private ArrayList <Integer> inBoundPathsNums;
    private ArrayList <Integer> inRoutePathsNums;
    private ArrayList <ArrayList <Integer>> indexList;
    private GoogleMap bikeMap;
    private double northest, southest, eastest, westest;

    public BikePathCalculator (PolylineOptions currentRoutePolylineOpt, LatLngBounds currentRouteBounds,
                               ArrayList <Polyline> iriaPaths, ArrayList <PolylineOptions> iriaPathsPolyLineOpt, GoogleMap mMap, DirectionsRoute currDirectionRoute){
        routePolylineOpt = currentRoutePolylineOpt;
        routeBounds = currentRouteBounds;
        iriaPathsList = iriaPaths;
        directionsRoute = currDirectionRoute;
        bikeMap = mMap;
        resultPathsList = new ArrayList<>();
        iriaPathsListPolyLineOpt = iriaPathsPolyLineOpt;
        inBoundPathsNums = new ArrayList<Integer>();
        inRoutePathsNums = new ArrayList<Integer>();
        totalInRoutePaths = new ArrayList<>();
        resultPartialPathList = new ArrayList<>();
        //initIndexList();
        //manualCalcBounds();
        //calcPathsNumberInBounds();
        //getPathsInRoute();
        getBikePathPercentageByRoute(3);
        //drawBikePathInRoute();
        newestDrawBikePathInRoute();
    }

    ////////////////////////////////////calculations options///////////////////////////////////////
    public void getBikePathPercentageByRoute (int calcMethod){
        float bikePathDistance;
        float bikePathPercentage;
        long routeDistance = calcCurrRouteDistance();
        System.out.println("distance: " + routeDistance);
        if (calcMethod == 1){
            bikePathDistance = calcTotalBikePathDitance();
            System.out.println("distanceeeeeeeeeeeeeeeeeeee: " + bikePathDistance);
            bikePathPercentage = calcBikePathPercentage(routeDistance, bikePathDistance);
            System.out.println("percentage: " + bikePathPercentage);
        }

        if (calcMethod == 2){
            bikePathDistance = newCalcTotalBikePathDitance();
            System.out.println("distance new: " + bikePathDistance);
            bikePathPercentage = calcBikePathPercentage(routeDistance, bikePathDistance);
            System.out.println("percentage new: " + bikePathPercentage);
        }

        if (calcMethod == 3){
            calcOverlapByRoute();
            bikePathDistance = calcBikePathDitance();
            bikePathPercentage = calcBikePathPercentage(routeDistance, bikePathDistance);
            System.out.println("percentage accurate: " + bikePathPercentage);
        }

        /*if (toDraw == 1){
            BikePathCalculator pathCalculator = new BikePathCalculator(allRoutes.getAllRoutes().get(0).routePolylineOptions,
                    directionsManager.getDirectionBounds(), iriaBikePathList, iriaPathPolyLineOpt, mMap, allRoutes.getAllRoutes().get(0).directionsRoute);
            System.out.println("inside: " + toDraw);
            pathCalculator.drawBikePath(pathNum);
            pathNum++;
            return;

            toDraw = 1;
        }*/
    }
    ///////////////////////////////////calculation options///////////////////////

    public void getPathsInRoute (){
        for (Integer pathNum : inBoundPathsNums){
            if (isPathIn(iriaPathsList.get(pathNum))){
                inRoutePathsNums.add(pathNum);
                if (newConditionCheck(iriaPathsListPolyLineOpt.get(pathNum))) {
                    totalInRoutePaths.add(iriaPathsListPolyLineOpt.get(pathNum));
                }
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
        midPointIn3 = checkPoint(bikePathPoints.get(((bikePathPoints.size())/4)*3));


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
        if (numOfPointsIn >= 2){
            resultPartialPathList.add(partialPath);
            if (newConditionCheck(partialPath)) {
                totalInRoutePaths.add(partialPath);
            }
        }
    }

    public boolean checkPoint (LatLng point){
        if (PolyUtil.isLocationOnPath(point, routePolylineOpt.getPoints(), true, 35)){
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

    public void calcPathsNumberInBounds(){
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

    public void drawBikePath (int pathNum){
        System.out.println("bounds size: " + inBoundPathsNums.size());
        System.out.println("path number: " + inBoundPathsNums.get(pathNum));
        PolylineOptions bikePathPolyLineOpt;
        ArrayList <LatLng> bikePathPoints = (ArrayList) iriaPathsList.get(inBoundPathsNums.get(pathNum)).getPoints();
        bikePathPolyLineOpt = new PolylineOptions();
        for (LatLng point : bikePathPoints){
            bikePathPolyLineOpt.add(point);
        }
        bikePathPolyLineOpt.color(Color.MAGENTA);
        bikeMap.addPolyline(bikePathPolyLineOpt);
    }


    public ArrayList<Polyline> getResultPathsList() {
        return resultPathsList;
    }
    public void setResultPathsList(ArrayList<Polyline> result) {
        resultPathsList = result;
    }

    public ArrayList <Integer> getInBoundPathsNums (){
        return inBoundPathsNums;
    }

    public ArrayList <Integer> getInRoutePathsNums (){
        return inRoutePathsNums;
    }

    public float otherCalc (){
        int numPoints = 0;
        ArrayList<LatLng> points = (ArrayList) routePolylineOpt.getPoints();
        int numTotalPoints = points.size();
        for (LatLng point : points) {
            for (Polyline line : iriaPathsList){
                if (checkPointWithPath(point, line)){
                    numPoints++;
                    break;
                }
            }
        }
        return (float)numPoints/(float)numTotalPoints;

    }

    public void calcOverlapByRoute (){
        boolean isInBikePath;
        ArrayList<LatLng> points = (ArrayList) routePolylineOpt.getPoints();
        PolylineOptions currPolyLineOpt = new PolylineOptions();
        for (LatLng point : points) {
            isInBikePath = false;
            for (Polyline line : iriaPathsList){
                if (checkPointWithPath(point, line)){
                    isInBikePath = true;
                    break;
                }
            }
            if(isInBikePath){
                currPolyLineOpt.add(point);
            }
            else{
                if (currPolyLineOpt.getPoints().size() > 1){
                    totalInRoutePaths.add(currPolyLineOpt);
                }
                currPolyLineOpt = new PolylineOptions();

            }
        }
        if (currPolyLineOpt.getPoints().size() > 1){
            totalInRoutePaths.add(currPolyLineOpt);
        }

    }

    public boolean checkPointWithPath (LatLng point, Polyline line){
        if (PolyUtil.isLocationOnPath(point, line.getPoints(), true, 50)){
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

    public boolean newConditionCheck(PolylineOptions path){
        int closestIndexToStartPath;
        int closestIndexToEndPath;
        ArrayList <LatLng> points = (ArrayList) path.getPoints();
        closestIndexToStartPath = findClosestPoint(points.get(0));
        closestIndexToEndPath = findClosestPoint(points.get(points.size() - 1));
        if (closestIndexToStartPath > closestIndexToEndPath){
            int temp = closestIndexToStartPath;
            closestIndexToStartPath = closestIndexToEndPath;
            closestIndexToEndPath = temp;
        }

        if (indexList.get(closestIndexToStartPath).size() != 0){
            for (Integer pathIndex: indexList.get(closestIndexToStartPath)){
                if (checkIfPathParallel (path, totalInRoutePaths.get(pathIndex))){
                    return false;
                }

            }
        }
        if (indexList.get(closestIndexToEndPath).size() != 0){
            for (Integer pathIndex: indexList.get(closestIndexToEndPath)){
                if (checkIfPathParallel (path, totalInRoutePaths.get(pathIndex))){
                    return false;
                }
            }
        }

        fillIndexListBetweenStartAndEnd(closestIndexToStartPath, closestIndexToEndPath);

        return true;
    }

    public boolean checkIfPathParallel (PolylineOptions path1, PolylineOptions path2){
        LatLng startPoint1 = path1.getPoints().get(0);
        LatLng endPoint1 = path1.getPoints().get(path1.getPoints().size()- 1);
        LatLng startPoint2 = path2.getPoints().get(0);
        LatLng endPoint2 = path2.getPoints().get(path2.getPoints().size() - 1);
        double start1Lat = startPoint1.latitude;
        double start1Lon = startPoint1.longitude;
        double end1Lat = endPoint1.latitude;
        double end1Lon = endPoint1.longitude;
        double start2Lat = startPoint2.latitude;
        double start2Lon = startPoint2.longitude;
        double end2Lat = endPoint2.latitude;
        double end2Lon = endPoint2.longitude;
        if (twoPointsDistance(startPoint1, endPoint1) < twoPointsDistance(startPoint2, endPoint2)){
            if ((start2Lat - end2Lat) > (start2Lon - end2Lon)){
                if (start2Lat > end2Lat){
                    double temp = start2Lat;
                    start2Lat = end2Lat;
                    end2Lat = temp;
                }
                if ((start1Lat > start2Lat && start1Lat < end2Lat) || (end1Lat > start2Lat && end1Lat < end2Lat)){
                    return true;
                }
                else{
                    return false;
                }
            }
            else{
                if (start2Lat > end2Lat){
                    double temp = start2Lon;
                    start2Lon = end2Lon;
                    end2Lon = temp;
                }
                if ((start1Lon > start2Lon && start1Lon < end2Lon) || (end1Lon > start2Lon && end1Lon < end2Lon)){
                    return true;
                }
                else{
                    return false;
                }
            }
        }

        else{
            if ((start1Lat - end1Lat) > (start1Lon - end1Lon)){
                if (start1Lat > end1Lat){
                    double temp = start1Lat;
                    start1Lat = end1Lat;
                    end1Lat = temp;
                }
                if ((start2Lat > start1Lat && start2Lat < end1Lat) || (end2Lat > start1Lat && end2Lat < end1Lat)){
                    return true;
                }
                else{
                    return false;
                }
            }
            else{
                if (start1Lat > end1Lat){
                    double temp = start1Lon;
                    start1Lon = end1Lon;
                    end1Lon = temp;
                }
                if ((start2Lon > start1Lon && start2Lon < end1Lon) || (end2Lon > start1Lon && end2Lon < end1Lon)){
                    return true;
                }
                else{
                    return false;
                }
            }
        }
    }

    public void fillIndexListBetweenStartAndEnd (int startIndex, int endIndex){
        for (int i = startIndex; i < endIndex + 1; i++){
            indexList.get(i).add(totalInRoutePaths.size());
        }
    }

    public int findClosestPoint (LatLng pathPoint){
        int closestPoint = 0;
        double closestDistance = 1000000;
        double currDistance = 0;
        LatLng routePoint;
        ArrayList<LatLng> routePoints = (ArrayList) routePolylineOpt.getPoints();
        for (int i = 0; i < routePoints.size(); i++){
            routePoint = routePoints.get(i);
            currDistance = twoPointsDistance(routePoint, pathPoint);
            if (currDistance < closestDistance){
                closestDistance = currDistance;
                closestPoint = i;
            }
        }
        return closestPoint;
    }

    public double twoPointsDistance (LatLng point1, LatLng point2){
        double x1, x2, y1, y2;
        x1 = point1.longitude; x2 = point2.longitude; y1 = point1.latitude; y2 = point2.latitude;
        return Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
    }

    public void initIndexList(){
        indexList = new ArrayList<>();
        for (int i = 0; i < routePolylineOpt.getPoints().size(); i++){
            ArrayList < Integer> addedList = new ArrayList<>();
            indexList.add(addedList);
        }
    }

    public float newCalcTotalBikePathDitance (){
        float distance = 0;
        ArrayList <LatLng> pathPoints;
        for (PolylineOptions path : totalInRoutePaths){
            pathPoints = (ArrayList)path.getPoints();
            for (int i = 0; i < pathPoints.size() - 1; i++){
                distance += calcDistanceBetweenTwoPoints(pathPoints.get(i), pathPoints.get(i+1));
            }
        }

        return distance;
    }

    public void newestDrawBikePathInRoute(){
        for (PolylineOptions bikePath : totalInRoutePaths){
            bikePath.color(Color.MAGENTA);
            bikeMap.addPolyline(bikePath);
        }
    }
}
