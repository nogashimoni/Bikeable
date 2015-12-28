package com.nnys.bikeable;


import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class FinalScoreAnalyzer extends AppCompatActivity {


    private GeoApiContext context;
    private List<Double> RandomElevationScoress;
    String filename;
    List<DirectionsRoute> directionsRoutes;
    int sampleSize;

    public FinalScoreAnalyzer() {
        this.sampleSize = 300;
        this.context = new GeoApiContext().setApiKey("AIzaSyBq4x4t8-j30Vbo5jrax_jIMkkMTlZdp1k");;
        this.directionsRoutes = generateRandomRoutes();
        this.RandomElevationScoress = calculateTheSampleElevationScores();
        this.filename = "ElevationAlgorithmResults.csv";
    }

    private List<Double> calculateTheSampleElevationScores() {
        List<Double>  pathElevationScores = new ArrayList<Double>() ;

        for ( DirectionsRoute directionsRoute: directionsRoutes) {
            PathElevationQuerier elevationQuerier = new PathElevationQuerier(directionsRoute.overviewPolyline);
            long distance = calcPathLength(directionsRoute);
            int numOfSamples = elevationQuerier.calcNumOfSamplesForXmetersIntervals(distance, BikeableRoute.GRAPH_X_INTERVAL, BikeableRoute.MAX_GRAPH_SAMPLES);
            if ( numOfSamples == 0 ) numOfSamples = 1;
            ElevationResult[] results = elevationQuerier.getElevationSamples(numOfSamples);
            PathElevationScoreCalculator scoreCalculator = new PathElevationScoreCalculator(results, distance);
            pathElevationScores.add(scoreCalculator.getPathScore());
        }
        return pathElevationScores;
    }

    private List<DirectionsRoute> generateRandomRoutes() {
        List<DirectionsRoute>  directionsRoutes = new ArrayList<DirectionsRoute>() ;

        while (directionsRoutes.size() < sampleSize) {
            LatLng fromLatLng = createRandomLatLngInTLV();
            LatLng toLatLng = createRandomLatLngInTLV();
            if (fromLatLng.longitude == toLatLng.longitude && fromLatLng.latitude == toLatLng.latitude) {
                continue;
            }
            DirectionsRoute[] calculatedRoutes = null;

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

            if ( calculatedRoutes != null ) {
                for ( DirectionsRoute calculatedRoute : calculatedRoutes) {
                    directionsRoutes.add(calculatedRoute);
                }
            }
        }

        while ( directionsRoutes.size() > sampleSize) {
            directionsRoutes.remove(directionsRoutes.size()-1);
        }

        return directionsRoutes;
    }



    public void printResults() {

        for (double score : RandomElevationScoress) {
            Log.d("DEBUG", String.format("SCORE: %f \n", score));
        }
    }

    private static long calcPathLength(DirectionsRoute directionsRoute) {
        long distance = 0;
        for (DirectionsLeg leg : directionsRoute.legs) {
            distance += leg.distance.inMeters;
        }
        return distance;
    }
    private static LatLng createRandomLatLngInTLV() {
//        LatLng rightMost = new LatLng(32.073908, 34.797249);
//        LatLng leftMost = new LatLng(32.091383, 34.772950);
//        LatLng downMost = new LatLng(32.037536, 34.771156);
//        LatLng upMost = new LatLng(32.130038, 34.819908);

        // generate a random lat between  32.037536 and 32.130038
        // double randomLat = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        Random r1 = new Random();
        double randomLat = 32.037536 + (32.130038 - 32.037536) * r1.nextDouble();

        // generate a random lng between 34.772950 and 34.797249
        Random r2 = new Random();
        double randomLng = 34.772950 + (34.797249 - 34.772950) * r2.nextDouble();

        return new LatLng(randomLat, randomLng);
    }

    private static LatLng createRandomLatLngXMetersNearArg(LatLng positonLatLng) {


        // generate a random lat between  32.037536 and 32.130038
        // double randomLat = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        Random r1 = new Random();
        double randomLat = 32.037536 + (32.130038 - 32.037536) * r1.nextDouble();

        // generate a random lng between 34.772950 and 34.797249
        Random r2 = new Random();
        double randomLng = 34.772950 + (34.797249 - 34.772950) * r2.nextDouble();

        return new LatLng(randomLat, randomLng);
    }

}
