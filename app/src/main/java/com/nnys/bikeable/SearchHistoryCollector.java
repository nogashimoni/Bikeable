package com.nnys.bikeable;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class SearchHistoryCollector {

    private final int MAX_HISTORY_RECORDS = 10;
    private File historyFile;
    private String historyFileName;
    private Context appContext;
    private GeoApiContext geoApiContext;
    private LinkedList<AutocompletePrediction> onlineHistory;


    public SearchHistoryCollector(Context appContext, GeoApiContext geoApiContext) {

        historyFileName = "Bikeable_History";
        this.appContext = appContext;
        this.geoApiContext = geoApiContext;
        onlineHistory = new LinkedList<>();
        historyFile = new File(appContext.getFilesDir(), historyFileName);

        // create a new file if it doesn't exist
        try {
            historyFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("INFO:", String.format("Before load history size %d", onlineHistory.size()));
        loadSearchHistory();
        Log.i("INFO:", String.format("After load history size %d", onlineHistory.size()));
    }

    public void addPredictionToHistory(AutocompletePrediction prediction){
        // will be used after search
        if (prediction == null || prediction.getPlaceId() == null){
            return;
        }

        boolean isInHistory = isPredictionInOnlineHistory(prediction);
        if (isInHistory) {
            return;
        }

        addPredictionToOnlineHistory(prediction);

        updateToOfflineHistory();
    }

    private void addPredictionToOnlineHistory(AutocompletePrediction prediction){
        onlineHistory.addFirst(prediction);
        if (onlineHistory.size() > MAX_HISTORY_RECORDS){
            onlineHistory.removeLast();

        }
    }

    private boolean isPredictionInOnlineHistory(AutocompletePrediction prediction){
        Log.i("INFO:", String.format("online history size: %d", onlineHistory.size()));
        for (AutocompletePrediction onlinePrediction: onlineHistory){
            if (onlinePrediction.getPlaceId().equals(prediction.getPlaceId())){
                return true;
            }
        }
        return false;
    }

    private void updateToOfflineHistory() {

        FileOutputStream outputStream;

        try {
            // delete file content
            PrintWriter writer = new PrintWriter(historyFile);
            writer.print("");
            writer.close();

            outputStream = appContext.openFileOutput(historyFileName, Context.MODE_APPEND); //todo MODE_PRIVATE ?
            for (AutocompletePrediction prediction : onlineHistory){
                outputStream.write(prediction.getPlaceId().concat("\n").getBytes());
            }
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSearchHistory() {
        String line, placeId;
        PlaceDetails placeDetails;

        try {
            FileInputStream fis = appContext.openFileInput(historyFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);


            while ((line = bufferedReader.readLine()) != null) {
                placeId = line.replaceAll("\n", "");
                Log.i("INFO line:", placeId);
                placeDetails = PlacesApi.placeDetails(geoApiContext, placeId).await();
                onlineHistory.add(new CustomAutoCompletePrediction(
                        placeDetails.name, "loaded from history", placeId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LinkedList<AutocompletePrediction> getOnlineHistory() {
        return onlineHistory;
    }
}
