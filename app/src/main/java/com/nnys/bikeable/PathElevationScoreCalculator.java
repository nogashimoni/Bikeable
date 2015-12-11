package com.nnys.bikeable;

import com.google.maps.model.ElevationResult;
import com.google.maps.model.EncodedPolyline;

import static java.lang.Math.atan;

public class PathElevationScoreCalculator {

    ElevationResult[] elevationResults;
    long length;
    int numOfSamples;
    int xDelta;


    public PathElevationScoreCalculator(ElevationResult[] elevationResults, long length){
        this.elevationResults = elevationResults;
        this.length = length;
        this.xDelta = PathElevationQuerier.getDistanceBetweenSamples(length);
        this.numOfSamples = PathElevationQuerier.calcNumOfSamplesForXmetersIntervals(length, CentralActivity.GRAPH_X_INTERVAL, CentralActivity.MAX_GRAPH_SAMPLES);
    }

    //The bigger the path result is, the harder the path is
    public double getPathScore(){
        double[] degreesArray = createDegreesArray();
        double result = 0;
        for (double degree : degreesArray){
            result += calculateDegreeScore(degree);
        }

        return result;
    }

    private double calculateDegreeScore(double slop) {
        double result = slop/90;

        return result*100;
    }

    private double[] createDegreesArray() {
        double[] degreesArray;
        degreesArray = new double[numOfSamples-1];
        for (int i=0; i<numOfSamples-1; i++){
            degreesArray[i] = calculateSlopDegree(elevationResults[i].elevation, elevationResults[i+1].elevation);
        }
        return degreesArray;
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


}
