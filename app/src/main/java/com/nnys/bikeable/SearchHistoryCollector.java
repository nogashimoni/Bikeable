package com.nnys.bikeable;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nogal on 30/12/2015.
 */
public class SearchHistoryCollector {
    File historyFile;
    String historyFileName;
    Context context;

    public SearchHistoryCollector(Context context) {
        historyFileName = "Bikeable_History";
        this.context = context;
//        historyFile = new File(context.getFilesDir(), historyFileName);
    }

    public void addPlaceId(String placeID) {
        FileOutputStream outputStream;

        try {
            String placeIDLine = placeID.concat("\n");
            outputStream = context.openFileOutput(historyFileName, Context.MODE_APPEND); //todo MODE_PRIVATE ?
            outputStream.write(placeIDLine.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getSearchHistory() {
        List <String> result = new ArrayList<String>();
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
