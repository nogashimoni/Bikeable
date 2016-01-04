package com.nnys.bikeable;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;

import com.google.maps.model.ElevationResult;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.ArrayList;

public class PathElevationGraphDrawer extends AppCompatActivity {
    GraphView graph;
    List<ElevationResult[]> allElevationResults;
    int selectedSeriesIndex;
    ArrayList<LineGraphSeries<DataPoint>> pathsSeries;

    public PathElevationGraphDrawer(GraphView graph) {
        this.graph = graph;
        allElevationResults = new ArrayList<>();
        setGraph(graph);
        pathsSeries = new ArrayList<>();
    }

    private void setGraph(GraphView graph) {
        graph.removeAllSeries();
        graph.getLegendRenderer().setVisible(false);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(80); //has to divide by for in order that x-axis legand will show integers
        graph.setBackgroundColor(Color.rgb(249, 255, 255));
        formatXAxix(graph);

    }


    public void addSeries( ElevationResult[] elevationResults, int seriesIndex ) {
        DataPoint[] points;
        if (elevationResults == null){
            points = new DataPoint[1];
            points[0] = new DataPoint(0, 0);
        }
        else {
            points = new DataPoint[elevationResults.length];
            for (int i = 0; i < elevationResults.length; i++) {
                points[i] = new DataPoint(i, elevationResults[i].elevation);
            }
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(points);
        series.setThickness(10);
        series.setColor(Color.BLACK);
        series.setTitle(String.format("%d", seriesIndex));

        allElevationResults.add(elevationResults);
        pathsSeries.add(series);
        graph.addSeries(series);
    }

    private void formatXAxix(GraphView graph) {
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // new value is sample_num*x_interval_size(from values)
                    return super.formatLabel(value * BikeableRoute.GRAPH_X_INTERVAL, isValueX);
                } else {
                    // show normal y values
                    return super.formatLabel(value, isValueX);

                }
            }
        });
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Distance (meters)");
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.rgb(48, 139, 159));
        graph.getGridLabelRenderer().setVerticalAxisTitle("Elevation (meters)");
        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.rgb(48, 139, 159));

    }

    public void setSelectedSeriesAndColorIt(int index) {
        selectedSeriesIndex = index;
        // first remove all series
        graph.removeAllSeries();
        LineGraphSeries<DataPoint> currSeries;
        // now add all serieses but index
        for (int j = 0; j < pathsSeries.size(); j++) {
            if (j == index)
                continue;
            currSeries = pathsSeries.get(j);
            currSeries.setColor(Color.BLACK);
            graph.addSeries(currSeries);
            graph.invalidate();
        }

        currSeries = pathsSeries.get(index);
        currSeries.setColor(Color.rgb(48, 139, 159));
        graph.addSeries(currSeries);
        graph.invalidate();
    }

    public int getSelectedSeriesIndex() {
        return selectedSeriesIndex;
    }
}
