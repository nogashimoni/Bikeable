package com.nnys.bikeable;

import com.google.maps.model.ElevationResult;
import com.google.maps.model.EncodedPolyline;

import static java.lang.Math.atan;
import static java.lang.Math.log;

/**
 * This is a class that calculates a score for a path.
 * The score is calculated by the function "get path score"
 * Usage example:
 *
 PathElevationScoreCalculator pathElevationScoreCalculator = new PathElevationScoreCalculator(
    results, distance);
 double score = pathElevationScoreCalculator.getPathScore();
 double uphillPresentage = pathElevationScoreCalculator.getUphillPresentage();
 double uphillAbove5Presentage = pathElevationScoreCalculator.getUphillAbove5degrees();

 Log.i("INFO:", String.format("Path score is %f", score);
 Log.i("INFO:", String.format("Path uphill percentage is %f", uphillPresentage);
 Log.i("INFO:", String.format("Path uphill above 5 degrees percentage is %f",
    uphillAbove5Presentage);
 */
public class PathElevationScoreCalculator {

    ElevationResult[] elevationResults;
    long length;
    int numOfSamples;
    int xDelta;
    double[] degreesArray;


    public PathElevationScoreCalculator(ElevationResult[] elevationResults, long length){
        this.elevationResults = elevationResults;
        this.length = length;
        this.xDelta = PathElevationQuerier.getDistanceBetweenSamples(length);
        this.numOfSamples = PathElevationQuerier.calcNumOfSamplesForXmetersIntervals(length, CentralActivity.GRAPH_X_INTERVAL, CentralActivity.MAX_GRAPH_SAMPLES);
        this.createDegreesArray();
    }

    //The bigger the path result is, the harder the path is
    public double getPathScore(){
        double result = 0;
        for (double degree : degreesArray){
            result += calculateDegreeScore(degree);
        }

        return result;
    }

    private double calculateDegreeScore(double slop) {
        if (slop > 30){
            return 100;
        }
        double result = slop/30;
        return result*100;
    }

    private void createDegreesArray() {
        degreesArray = new double[numOfSamples-1];
        for (int i=0; i<numOfSamples-1; i++){
            degreesArray[i] = calculateSlopDegree(elevationResults[i].elevation, elevationResults[i+1].elevation);
        }
    }

    private double calculateSlopDegree(double start, double end){

        double result = 0;
        double slop = (end - start)/xDelta;
        if (slop < 0){
            return result;
        }
        result = atan(slop);
        return result;

    }

    public double getUphillPresentage(){

        int numOfUphill = 0;
        for (double slop : degreesArray) {
            if (slop > 0){
                numOfUphill++;
            }
        }
        return ((numOfUphill*100)/degreesArray.length);
    }

    public double getUphillAbove5degrees(){

        int numOfUphill = 0;
        for (double slop : degreesArray) {
            if (slop > 5){
                numOfUphill++;
            }
        }
        return ((numOfUphill*100)/degreesArray.length);
    }


}
