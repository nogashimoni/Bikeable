package com.nnys.bikeable;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.model.ElevationResult;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by nogal on 30/12/2015.
 */
public class GraphToMapConnector {

    PathElevationGraphDrawer graphDrawer;
    GoogleMap googleMap;
    AllRoutes allRoutes;

    public GraphToMapConnector(PathElevationGraphDrawer graphDrawer, GoogleMap googleMap) {
        this.graphDrawer = graphDrawer;
        this.googleMap = googleMap;

    }

    public void connect() {
        if ( graphDrawer == null || googleMap == null ) {
            return; //nothing to connect
        }
        setOnGraphTapListeners();
    }



    private void setOnGraphTapListeners() {
        for (Series<DataPoint> series: graphDrawer.graph.getSeries()) {
            series.setOnDataPointTapListener(new MyTapListener());
        }
    }

    private class MyTapListener implements OnDataPointTapListener {
        @Override
        public void onTap(Series series, DataPointInterface dataPoint) {
            int tappedSeriesIndex = Integer.parseInt(series.getTitle()); //series index

            if ( tappedSeriesIndex != graphDrawer.getSelectedSeriesIndex() ) { // make only selected route clickable
                return;
            }

            int tappedDatePointIndex = 0;
            Iterator<DataPoint> iter = series.getValues(0, 100000); //TODO max int
            int dataPointIndex = 0;
            while ( iter.hasNext() ) {
                if ( dataPoint.equals(iter.next())) {
                    tappedDatePointIndex = dataPointIndex;
                    break;
                }
                dataPointIndex ++;
            }
            ElevationResult tappedElevationResult = graphDrawer.allElevationResults.get(tappedSeriesIndex)[tappedDatePointIndex];
            com.google.maps.model.LatLng tappedLatLng = tappedElevationResult.location;


            // Instantiates a new CircleOptions object and defines the center and radius
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(tappedLatLng.lat, tappedLatLng.lng))
                    .radius(50) // In meters
                    .fillColor(0x30FFFF00)
                    .strokeColor(Color.BLACK)
                    .strokeWidth(10)
                    .zIndex(1000); //todo max Z index

            // Get back the mutable Circle

            Circle circle = googleMap.addCircle(circleOptions);

//            circle.remove(); // todo remove after a while

//            double smallSize = circle.getRadius();
//            sleepForAnimation(1);
//            circle.setRadius(2 * smallSize);
//            sleepForAnimation(3);
//            circle.setRadius(smallSize);
//            sleepForAnimation(2);
//            circle.remove();

//            googleMap.addCircle(new CircleOptions().center(MapUtils.getGmsLatLngFromModel(tappedLatLng)).radius(10000));
        }


    }
}
