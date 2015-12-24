package com.nnys.bikeable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
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
public class IriaBikePath {

    static Boolean isBikePathShown;
    static Boolean isListInitialized;
    static ArrayList<Polyline> bikePathsTLV;
    private static ArrayList <LatLng> telOfanStationsList;
    private static ArrayList<PolylineOptions> bikePathPolylinesOpts;


    // TODO: can we initialize when app starts?
    public static void getIriaBikePath(GoogleMap mMap) throws IOException, XmlPullParserException {
        isBikePathShown = false;
        bikePathsTLV = new ArrayList<>();
        telOfanStationsList = new ArrayList<>();
        URL bikePathLayerUrl = new URL ("http://gisn.tel-aviv.gov.il/wsgis/service.asmx/GetLayer?layerCode=577&layerWhere=&xmin=&ymin=&xmax=&ymax=&projection=wgs84");
        URL telOfanLayerUrl = new URL ("http://gisn.tel-aviv.gov.il/wsgis/service.asmx/GetLayer?layerCode=835&layerWhere=&xmin=&ymin=&xmax=&ymax=&projection=wgs84");
        String bikeJsonWGS84 = getBikeLayerJsonStr(bikePathLayerUrl);
        bikePathPolylinesOpts = IriaJson.getPolylinesFromJsonStr(bikeJsonWGS84);
        // TODO: Add the line with a different z and width
        for (PolylineOptions line : bikePathPolylinesOpts) {
            line.visible(false);
            bikePathsTLV.add(mMap.addPolyline(line));
        }
        isListInitialized = true;

        String telOfanJsonWGS84 = getBikeLayerJsonStr(telOfanLayerUrl);
        telOfanStationsList =
                IriaJson.getStationsFromJsonStr(telOfanJsonWGS84);
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


    public static void showBikePathOnMap() {
        if (!isListInitialized){
            return;
        }
        for (Polyline line : bikePathsTLV){
            line.setVisible(true);
        }
        isBikePathShown = true;
    }


    public static void removeBikePathFromMap() {
        if (!isListInitialized) {
            return;
        }
        for (Polyline line : bikePathsTLV) {
            line.setVisible(false);
        }
        isBikePathShown = false;
    }

    public static boolean getIsBikePathShown(){
        return isBikePathShown;
    }
    public static ArrayList<Polyline> getBikePathsTLV (){
        return bikePathsTLV;
    }

    public static ArrayList<PolylineOptions> getBikePathsTLVPolyLineOpt (){
        return bikePathPolylinesOpts;
    }

    public static ArrayList <LatLng> getTelOfanStationsList (){
        return telOfanStationsList;
    }
}
