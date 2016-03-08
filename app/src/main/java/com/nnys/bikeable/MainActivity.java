package com.nnys.bikeable;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

// dynamoDB imports
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;


/**
 * This activity is the main activity that launches on app startup.
 * It is responsible for getting the data from the Iria before the app launches.
 */
public class MainActivity extends AppCompatActivity {
    private DynamoDBMapper mapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        ImageView img = (ImageView)findViewById(R.id.bike_animation_image);
        img.setBackgroundResource(R.drawable.bike_anim);
        AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
        frameAnimation.start();

        // to enable getting data from Tel Aviv muni website
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // run data retrieval from Iria, and then run centralActivity
        BackgroundTask task = new BackgroundTask(MainActivity.this);
        task.execute();

    }

    // put as a private class inside the activity class
    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        MainActivity activity;

        public BackgroundTask(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Void result) {
            // Execute some code after 2 seconds have passed
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(MainActivity.this, CentralActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return;
                }
            }, 1000);

        }

        @Override
        protected Void doInBackground(Void... params) {
            // init iria data
            IriaData.initIriaData();

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "eu-west-1:75f8b90a-9b52-485b-86c4-bc517b8ad22b", // Identity Pool ID
                    Regions.EU_WEST_1 // Region
            );
            AmazonDynamoDBClient ddbClient = Region.getRegion(Regions.EU_WEST_1)
                    .createClient(
                            AmazonDynamoDBClient.class,
                            credentialsProvider,
                            new ClientConfiguration()
                    );
            mapper = new DynamoDBMapper(ddbClient);

            // get iria data
            try {
                ConstantsFromTable bikePathLayerUrl = mapper.load(ConstantsFromTable.class, "bikePathLayerUrl");
                ConstantsFromTable telOfanLayerUrl = mapper.load(ConstantsFromTable.class, "telOfanLayerUrl");
                ConstantsFromTable telOFunSiteURL = mapper.load(ConstantsFromTable.class, "telOFunStationsURL");
                IriaData.getIriaData(bikePathLayerUrl, telOfanLayerUrl, telOFunSiteURL);
                IriaData.isDataReceived = true;
            } catch (Exception e) { // There was a problem getting the data from the Municipality site
                Log.i("INFO:", "in main activity Data from iria is NOT OK");
                IriaData.isDataReceived = false;
                e.printStackTrace();
            }

            return null;
        }

    }

}
