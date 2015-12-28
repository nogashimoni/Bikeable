package com.nnys.bikeable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

// add to main activity
// StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
// StrictMode.setThreadPolicy(policy);
// add to manifest?
// <uses-permission android:name="android.permission.INTERNET"/>

/**
 * Created by Yishay on 11/27/2015.
 */
public class IriaData {

    static boolean isBikePathShown;
    static boolean isBikePathPolylinesAdded;

    static boolean isTelOFunShown;
    static boolean isTelOFunMarkersAdded;

    static ArrayList<Polyline> bikePathsPolylines;
    static ArrayList<Marker> telOFunMarkers;

    private static ArrayList <LatLng> telOfanStationsList;
    private static ArrayList<PolylineOptions> bikePathPolylinesOpts;

    public static Boolean isDataReceived;

    // TODO: can we initialize when app starts?
    public static void getIriaData() throws IOException, XmlPullParserException {
        isBikePathShown = false;
        bikePathsPolylines = new ArrayList<>();
        telOfanStationsList = new ArrayList<>();
        URL bikePathLayerUrl = new URL ("http://gisn.tel-aviv.gov.il/wsgis/service.asmx/GetLayer?layerCode=577&layerWhere=&xmin=&ymin=&xmax=&ymax=&projection=wgs84");
        URL telOfanLayerUrl = new URL ("http://gisn.tel-aviv.gov.il/wsgis/service.asmx/GetLayer?layerCode=835&layerWhere=&xmin=&ymin=&xmax=&ymax=&projection=wgs84");
        String bikeJsonWGS84 = getBikeLayerJsonStr(bikePathLayerUrl);
        bikePathPolylinesOpts = IriaJson.getPolylinesFromJsonStr(bikeJsonWGS84);
        // TODO: Add the line with a different z and width
        String telOfanJsonWGS84 = getBikeLayerJsonStr(telOfanLayerUrl);
        telOfanStationsList = IriaJson.getStationsFromJsonStr(telOfanJsonWGS84);
    }

    public static String getBikeLayerJsonStr(URL url) throws IOException, XmlPullParserException {
        URL bikeLayerUrl = url;
        String xmlStr = UrlManager.getUrlResponse(bikeLayerUrl);
        InputStream xmlIS = new ByteArrayInputStream( xmlStr.getBytes());
        return(parseBikeXMLtoJson(xmlIS));

    }


    public static String parseBikeXMLtoJson(InputStream in) throws XmlPullParserException, IOException {
        String res = null;
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            res = parser.nextText();
        } finally {
            in.close();
        }
        return res;
    }

    public static void addTelOFunToMap (GoogleMap mMap) {
        if (isTelOFunMarkersAdded)
            return;
        telOFunMarkers = new ArrayList<>();
        for (LatLng station : telOfanStationsList) {
            telOFunMarkers.add(
                    mMap.addMarker(new MarkerOptions()
                            .position(station)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .visible(false)));
        }
        isTelOFunMarkersAdded = true;
    }

    public static void addBikePathToMap (GoogleMap mMap) {
        if (isBikePathPolylinesAdded)
            return;
        bikePathsPolylines = new ArrayList<>();
        for (PolylineOptions line : bikePathPolylinesOpts) {
            line.zIndex(20); // TODO: not hard coded
            line.width(5); // TODO: not hard coded
            line.visible(false);
            bikePathsPolylines.add(mMap.addPolyline(line));
        }
        isBikePathPolylinesAdded = true;
    }


    public static void showTelOFunOnMap() {
        if (!isTelOFunMarkersAdded){
            return;
        }
        for (Marker station : telOFunMarkers){
            station.setVisible(true);
        }
        isTelOFunShown = true;
    }


    public static void showBikePathOnMap() {
        if (!isBikePathPolylinesAdded){
            return;
        }
        for (Polyline line : bikePathsPolylines){
            line.setVisible(true);
        }
        isBikePathShown = true;
    }


    public static void removeTelOFunFromMap() {
        if (!isTelOFunMarkersAdded) {
            return;
        }
        for (Marker station : telOFunMarkers) {
            station.setVisible(false);
        }
        isTelOFunShown = false;
    }

    public static void removeBikePathFromMap() {
        if (!isBikePathPolylinesAdded) {
            return;
        }
        for (Polyline line : bikePathsPolylines) {
            line.setVisible(false);
        }
        isBikePathShown = false;
    }

    public static boolean isBikePathShown(){
        return isBikePathShown;
    }

    public static Boolean isTelOFunShown() {
        return isTelOFunShown;
    }

    public static ArrayList<Polyline> getBikePathsPolylines(){
        return bikePathsPolylines;
    }

    public static ArrayList<PolylineOptions> getBikePathsTLVPolyLineOpt (){
        return bikePathPolylinesOpts;
    }

    public static ArrayList <LatLng> getTelOfanStationsList (){
        return telOfanStationsList;
    }
}
