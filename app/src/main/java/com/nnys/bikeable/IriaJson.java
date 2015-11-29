package com.nnys.bikeable;

import android.graphics.Color;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sharon on 28/11/2015.
 */
public class IriaJson {

    public static ArrayList<PolylineOptions> getPolylinesFromJsonStr(String jsonStr){
        Gson gson = new Gson();
        System.out.println(gson.fromJson(jsonStr, IriaBikeData.class));
        IriaBikeData o = gson.fromJson(jsonStr, IriaBikeData.class);
        ArrayList<PolylineOptions> bikePathPolylineOpts = getAllPathsPolylines(o.getFeatures());
        return bikePathPolylineOpts;
    }

    public static ArrayList<PolylineOptions> getAllPathsPolylines(List<Feature> features){
        ArrayList<PolylineOptions> result = new ArrayList<>();
        PolylineOptions currPathPolylineOpts = null;
        for (Feature feature: features) {
            List<List<List<String>>> paths = feature.getGeometry().getPaths();
            for (List<List<String>> path : paths){
                currPathPolylineOpts = new PolylineOptions();
                currPathPolylineOpts.color(Color.GREEN);
                for (List<String> coordinate : path){
                    currPathPolylineOpts.add(new LatLng(
                            Double.parseDouble(coordinate.get(1)),
                            Double.parseDouble(coordinate.get(0))
                            ));
                }
                result.add(currPathPolylineOpts);
            }
        }
        System.out.println("hurray!");
        return result;
    }

//    static void printAllPaths(List<Feature> features){
//        String pathPartStr;
//
//        for (Feature feature: features) {
//            List<List<List<String>>> paths = feature.getGeometry().getPaths();
//
//            for (List<List<String>> PathList : paths){
//                System.out.println(String.format("%s", Arrays.toString(PathList.toArray())));
//
//            }
//        }
//    }
}



class IriaBikeData {
    private String displayFieldName;
    private List<Feature> features;

    public String getDisplayFieldName() {
        return displayFieldName;
    }

    public List<Feature> getFeatures() {
        return features;
    }
}


class Feature {
    private Geometry geometry;

    public Geometry getGeometry() {
        return geometry;
    }

}

class Geometry{
    private List<List<List<String>>> paths;

    public List<List<List<String>>> getPaths() {
        return paths;
    }
}



