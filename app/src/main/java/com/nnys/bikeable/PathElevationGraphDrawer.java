package com.nnys.bikeable;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;

import com.google.maps.model.ElevationResult;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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
    }


    public void addSeries( ElevationResult[] elevationResults ) {
        DataPoint[] points = new DataPoint[elevationResults.length];
        for ( int i=0; i < elevationResults.length; i++) {
            points[i] = new DataPoint(i, elevationResults[i].elevation);
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(points);
        series.setThickness(10);
        series.setColor(Color.BLACK);

        graph.addSeries(series);
    }

    public void colorSeriosByIndex( int i ) {
        LineGraphSeries<DataPoint> series = (LineGraphSeries<DataPoint>)graph.getSeries().get(i);
        series.setColor(Color.BLUE);
    }

}
