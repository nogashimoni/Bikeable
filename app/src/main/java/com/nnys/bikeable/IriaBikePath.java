package com.nnys.bikeable;

import com.google.android.gms.maps.GoogleMap;
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

    Boolean isBikePathShown;
    Boolean isListInitialized;
    ArrayList<Polyline> bikePathsTLV;


    // TODO: can we initialize when app starts?
    public IriaBikePath(GoogleMap mMap) throws IOException, XmlPullParserException {
        isBikePathShown = false;
        bikePathsTLV = new ArrayList<>();
        String bikeJsonWGS84 = getBikeLayerJsonStr();
        ArrayList<PolylineOptions> bikePathPolylinesOpts =
                IriaJson.getPolylinesFromJsonStr(bikeJsonWGS84);
        // TODO: Add the line with a different z and width
        for (PolylineOptions line : bikePathPolylinesOpts) {
            line.visible(false);
            bikePathsTLV.add(mMap.addPolyline(line));
        }
        isListInitialized = true;
    }

    public static String getBikeLayerJsonStr() throws IOException, XmlPullParserException {
        URL bikeLayerUrl = new URL ("http://gisn.tel-aviv.gov.il/wsgis/service.asmx/GetLayer?layerCode=577&layerWhere=&xmin=&ymin=&xmax=&ymax=&projection=wgs84");
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


    public void showBikePathOnMap() {
        if (!isListInitialized){
            return;
        }
        for (Polyline line : bikePathsTLV){
            line.setVisible(true);
        }
        isBikePathShown = true;
    }


    public void removeBikePathFromMap() {
        if (!isListInitialized) {
            return;
        }
        for (Polyline line : bikePathsTLV) {
            line.setVisible(false);
        }
        isBikePathShown = false;
    }
}
