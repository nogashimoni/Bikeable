package com.nnys.bikeable;

import java.util.ArrayList;

/**
 * Created by nleibovich on 01/01/2016.
 * This class creates a list of sections in a BikeableRoute, that have a significant uphill slope.
 */
public class ColorizeUphillSections {

    public final static int SIGNIFICANT_UPHILL_DEGREE = 5;
    BikeableRoute bikeableRoute;
    ArrayList<BikeableRoute> uphillSections;

    public ColorizeUphillSections(BikeableRoute bikeableRoute){
        this.bikeableRoute = bikeableRoute;
        this.uphillSections = getUphillSectionsAsBikeableRoute();
    }

    private ArrayList<BikeableRoute> getUphillSectionsAsBikeableRoute() {

        ArrayList<BikeableRoute> uphillSections = new ArrayList<>();
        double[] degreesArray = this.bikeableRoute.getDegreesArray();
        for (int i=0; i < degreesArray.length ; i++) {
            if (degreesArray[i] >= SIGNIFICANT_UPHILL_DEGREE){
                addUphillSection(uphillSections, i);
            }
        }
        return uphillSections;
    }

    private void addUphillSection(ArrayList<BikeableRoute> uphillSections, int i) {

    }
}
