package com.nnys.bikeable;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton button = (ImageButton) findViewById(R.id.startButton);


        // to enable getting data from Tel Aviv muni website
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // get iria data
        try {
            IriaData.getIriaData();
            IriaData.isDataReceived = true;
        } catch (IOException e) {
            IriaData.isDataReceived = false;
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            IriaData.isDataReceived = false;
            e.printStackTrace();
        }

        Log.i("INFO:", "Data from iria is OK");

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CentralActivity.class);
                startActivity(intent);
//            }
//        });
    }

}
