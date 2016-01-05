package com.nnys.bikeable;


import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.TravelMode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class ResearchPerformer extends AppCompatActivity {


    private GeoApiContext context;
    private List<Double> RandomElevationScoress;
    int randomRoutesMeters;
    List<DirectionsRoute> directionsRoutes;
    int sampleSize;
    private final int ALLOWED_MISTAKE = 40; // if we want teo lat langs with a distance of 500 meters route, we can get also 540 or 460

    public ResearchPerformer() {
        this.sampleSize = 300;
        this.context = new GeoApiContext().setApiKey("AIzaSyBq4x4t8-j30Vbo5jrax_jIMkkMTlZdp1k");;
        this.directionsRoutes = generateRandomRoutes();
        this.RandomElevationScoress = calculateTheSampleElevationScores();
    }

    private List<Double> calculateTheSampleElevationScores() {
        List<Double>  pathElevationScores = new ArrayList<Double>() ;

        for ( DirectionsRoute directionsRoute: directionsRoutes) {
            PathElevationQuerier elevationQuerier = new PathElevationQuerier(directionsRoute.overviewPolyline);
            long distance = calcPathLength(directionsRoute);
            int numOfSamples = elevationQuerier.calcNumOfSamplesForXmetersIntervals(distance, BikeableRoute.GRAPH_X_INTERVAL, BikeableRoute.MAX_GRAPH_SAMPLES);
            if ( numOfSamples == 0 ) {
                numOfSamples = 1;
            }
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
            LatLng toLatLng = getLatLngInTLVWithinXDistanceXMeters(randomRoutesMeters, fromLatLng); // will take time to calculate
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

    private LatLng getLatLngInTLVWithinXDistanceXMeters(int desiredMeters, LatLng fromLatLng) {
        LatLng result;

        while (true) {
            LatLng randomLatLng = createRandomLatLngInTLV();

            Location loc1 = new Location("");
            loc1.setLatitude(fromLatLng.latitude);
            loc1.setLongitude(fromLatLng.longitude);
            Location loc2 = new Location("");
            loc2.setLatitude(randomLatLng.latitude);
            loc2.setLongitude(randomLatLng.longitude);

            float distanceInMeters = loc1.distanceTo(loc2);
            if (  (distanceInMeters > desiredMeters - ALLOWED_MISTAKE ) && (distanceInMeters < desiredMeters + ALLOWED_MISTAKE) ) {
                result = new LatLng(randomLatLng.latitude, randomLatLng.longitude);
                Log.e("INFO", String.format("Found 2 lat langs that with distance of %f",distanceInMeters));
                return result;
            }
        }
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
