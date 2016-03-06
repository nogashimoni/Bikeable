package com.nnys.bikeable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is used for opening http connections for specified URLs and getting response
 */
public class UrlManager {

    protected static String getUrlResponse (URL url) throws IOException {
        String out = "";
        InputStream in = null;
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
            out = readStream(in);
        } finally {
            urlConnection.disconnect();
        }
        return out;
    }

    protected static String readStream(InputStream in){
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line = "";
        try {
            while((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
