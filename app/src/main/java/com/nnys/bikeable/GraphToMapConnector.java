package com.nnys.bikeable;

import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
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
                    .radius(60) // In meters
                    .fillColor(0x40FFFF00)
                    .strokeColor(Color.BLACK)
                    .strokeWidth(5)
                    .zIndex(1000); //todo max Z index

            // Get back the mutable Circle
            LatLng gmsLatLng = MapUtils.getGmsLatLngFromModel(tappedLatLng);
            Circle circle = googleMap.addCircle(circleOptions);
            boolean doesGraphHideCircle = googleMap.getProjection().toScreenLocation(gmsLatLng).y > 1000;
            if (!googleMap.getProjection().getVisibleRegion().latLngBounds.contains(gmsLatLng) || doesGraphHideCircle) {
                if (doesGraphHideCircle) {
                    Log.i("INFO", "graph hides circle so moving");
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(gmsLatLng));
            }
            removeCircleAfterSomeTime(circle);
        }


    }

    public void removeCircleAfterSomeTime(final Circle circle) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    circle.remove();
                }
            }
        });
    }
}
