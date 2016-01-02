package com.nnys.bikeable;

import android.graphics.Color;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static HashMap <Integer, TelOFunStation> getStationsFromJsonStr(String jsonStr){
        Gson gson = new Gson();
        System.out.println(gson.fromJson(jsonStr, IriaBikeData.class));
        IriaBikeData o = gson.fromJson(jsonStr, IriaBikeData.class);
        HashMap <Integer, TelOFunStation> stationsDict = getAllStations(o.getFeatures());
        return stationsDict;
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

    public static HashMap <Integer, TelOFunStation> getAllStations(List<Feature> features){
        HashMap <Integer, TelOFunStation> result = new HashMap<>();
        for (Feature feature: features){
            LatLng coordinates = new LatLng(Double.parseDouble(feature.getGeometry().getY()), Double.parseDouble(feature.getGeometry().getX()));
            int stationId = Integer.parseInt(feature.getAttributes().getStationId());
            int stationOrderId = Integer.parseInt(feature.getAttributes().getStationOid());
            TelOFunStation station = new TelOFunStation(coordinates, stationId, stationOrderId);
            result.put(stationId, station);
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
    private Attributes attributes;

    public Geometry getGeometry() {
        return geometry;
    }

    public Attributes getAttributes() {
        return attributes;
    }

}

class Attributes {
    private String oid_thana;
    private String tachana_id;

    public String getStationOid (){
        return oid_thana;
    }

    public String getStationId (){
        return tachana_id;
    }
}

class Geometry{
    private List<List<List<String>>> paths;
    private String x;
    private String y;

    public List<List<List<String>>> getPaths() {
        return paths;
    }
    public String getX () { return x; }
    public String getY () { return y; }
}



