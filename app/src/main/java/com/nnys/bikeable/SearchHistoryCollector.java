package com.nnys.bikeable;

import android.content.Context;

import com.google.android.gms.location.places.AutocompletePrediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class SearchHistoryCollector {

    final int MAX_HISTORY_RECORDS = 10;
    File historyFile;
    String historyFileName;
    Context context;
    LinkedList<AutocompletePrediction> onlineHistory;


    public SearchHistoryCollector(Context context) {

        historyFileName = "Bikeable_History";
        this.context = context;
        onlineHistory = new LinkedList<>();
        ArrayList<AutocompletePrediction> list = new ArrayList<>();

        historyFile = new File(context.getFilesDir(), historyFileName);
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

            outputStream = context.openFileOutput(historyFileName, Context.MODE_APPEND); //todo MODE_PRIVATE ?
            for (AutocompletePrediction prediction : onlineHistory){
                outputStream.write(prediction.getPlaceId().concat("\n").getBytes());
            }
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CustomAutoCompletePrediction> loadSearchHistory() {

        try {
            FileInputStream fis = context.openFileInput(historyFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
//            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.add(line.replaceAll("\n",""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


}
