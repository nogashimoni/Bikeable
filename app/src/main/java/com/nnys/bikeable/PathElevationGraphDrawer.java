package com.nnys.bikeable;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.maps.model.ElevationResult;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.NumberFormat;

public class PathElevationGraphDrawer extends AppCompatActivity {
    GraphView graph;
    ElevationResult[] elevationResults;

    public PathElevationGraphDrawer(GraphView graph) {
        this.graph = graph;
        setGraph(graph);
    }

    private void setGraph(GraphView graph) {
        graph.removeAllSeries();
        graph.getLegendRenderer().setVisible(false);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(80); //has to divide by for in order that x-axis legand will show integers
        formatXAxix(graph);

    }


    public void addSeries( ElevationResult[] elevationResults ) {
        assert (elevationResults != null);
        DataPoint[] points = new DataPoint[elevationResults.length];
        for ( int i=0; i < elevationResults.length; i++) {
            points[i] = new DataPoint(i, elevationResults[i].elevation);
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(points);
        series.setThickness(10);
        series.setColor(Color.BLACK);

        graph.addSeries(series);
    }

    private void formatXAxix(GraphView graph) {
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
        @Override
        public String formatLabel(double value, boolean isValueX) {
            if (isValueX) {
                // new value is sample_num*x_interval_size(from values)
                return super.formatLabel(value*BikeableRoute.GRAPH_X_INTERVAL, isValueX);
            } else {
                // show normal y values
                return super.formatLabel(value, isValueX);
            }
        }
        });
    }

    public void colorSeriosByIndex( int i ) {
        for (int j=0; j<graph.getSeries().size(); j++) {
            LineGraphSeries<DataPoint> series = (LineGraphSeries<DataPoint>)graph.getSeries().get(j);
            series.setColor(Color.BLACK);
        }
        LineGraphSeries<DataPoint> series = (LineGraphSeries<DataPoint>)graph.getSeries().get(i);
        series.setColor(Color.BLUE);
        graph.invalidate();

    }

}
