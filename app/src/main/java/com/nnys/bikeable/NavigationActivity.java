package com.nnys.bikeable;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.map.SKAnimationSettings;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKCoordinateRegion;
import com.skobbler.ngx.map.SKMapCustomPOI;
import com.skobbler.ngx.map.SKMapInternationalizationSettings;
import com.skobbler.ngx.map.SKMapPOI;
import com.skobbler.ngx.map.SKMapSurfaceListener;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapViewHolder;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.navigation.SKAdvisorSettings;
import com.skobbler.ngx.navigation.SKNavigationListener;
import com.skobbler.ngx.navigation.SKNavigationManager;
import com.skobbler.ngx.navigation.SKNavigationSettings;
import com.skobbler.ngx.navigation.SKNavigationState;
import com.skobbler.ngx.positioner.SKCurrentPositionListener;
import com.skobbler.ngx.positioner.SKCurrentPositionProvider;
import com.skobbler.ngx.positioner.SKPosition;
import com.skobbler.ngx.positioner.SKPositionerManager;
import com.skobbler.ngx.routing.SKRouteAdvice;
import com.skobbler.ngx.routing.SKRouteInfo;
import com.skobbler.ngx.routing.SKRouteJsonAnswer;
import com.skobbler.ngx.routing.SKRouteListener;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.ngx.util.SKLogging;
import com.skobbler.ngx.versioning.SKMapUpdateListener;
import com.skobbler.ngx.versioning.SKVersioningManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class NavigationActivity extends AppCompatActivity implements SKPrepareMapTextureListener, SKMapUpdateListener, SKMapSurfaceListener, SKRouteListener, SKNavigationListener, SKCurrentPositionListener {

    /*
     * layout related fields
     */

    private Button returnButton;
    private TextView waitForLocationTextBox, nextAdviceTextBox;

    /**
     * route inforamtion
     */
    List<LatLng> routeLatLngs;

    /**
     * map related fields
     */
    private String mapResDirPath;
    private SKMapSurfaceView mapView;
    private SKMapViewHolder mapHolder;

    /**
     * tts provider
     */
    private TextToSpeech textToSpeechEngine = null;

    /**
     * navigation related fields
     */
    // navigation manager
    SKNavigationManager navigationManager;
    // Tells if a navigation is ongoing
    private boolean skToolsNavigationInProgress;
    // counts the consecutive received positions with an accuracy greater than 150
    private byte numberOfConsecutiveBadPositionReceivedDuringNavi;
    // Tells if a route calculation is finished
    private boolean skToolsRouteCalculated;


    /**
     * Current position related fields
     */
    // Current position provider
    private SKCurrentPositionProvider currentPositionProvider;
    // Current position
    private SKPosition currentPosition;
    // timestamp for the last currentPosition
    private long currentPositionTime;
    // tells if already got positions for the first time
    private boolean gotFirstCurrentLocation;
    //handler that checks during navigation after every 5 seconds whether a new gps position was
    // received or not
    private Handler gpsPositionsDelayChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the route info
        Bundle bundle = getIntent().getExtras();
       routeLatLngs = getIntent().getParcelableArrayListExtra("routeLatLngs");
        if (routeLatLngs == null){
            Log.i("INFO:", "route is null");
        }
        else {
            Log.i("INFO:", String.format("route is null not null!!! %d", routeLatLngs.size()));
        }
        // Added for current location
        currentPositionProvider = new SKCurrentPositionProvider(this);
        currentPositionProvider.setCurrentPositionListener(this);
        currentPositionProvider.requestLocationUpdates(true, true, false);

        // set external dirs
        File externalDir = getExternalFilesDir(null);
        if (externalDir != null) {
            mapResDirPath = externalDir + "/SKMaps_zip/";
        } else {
            mapResDirPath = getFilesDir() + "/SKMaps_zip/";
        }

        // prepare and start the map thread
        final SKPrepareMapTextureThread prepThread = new SKPrepareMapTextureThread(
                this, mapResDirPath, "SKMaps.zip", this);
        prepThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapHolder != null) {
            mapHolder.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapHolder != null){
            mapHolder.onResume();
        }
    }

    @Override
    public void onMapTexturesPrepared(boolean b) {

        // copy map resources
        SKVersioningManager.getInstance().setMapUpdateListener(this);
        Toast.makeText(
                NavigationActivity.this, "Map resources copied", Toast.LENGTH_SHORT).show();

        // initialize map settings
        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();
        initMapSettings.setMapResourcesPaths(
                mapResDirPath, new SKMapViewStyle(mapResDirPath + "/daystyle/", "daystyle.json"));
        initMapSettings.setPreinstalledMapsPath(mapResDirPath + "/PreinstalledMaps");
        SKMaps.getInstance().initializeSKMaps(this, initMapSettings);

        // set up advisor (text to speach)
        final SKAdvisorSettings advisorSettings = initMapSettings.getAdvisorSettings();
        advisorSettings.setAdvisorConfigPath(mapResDirPath + "/Advisor");
        advisorSettings.setResourcePath(mapResDirPath + "/Advisor/Languages");
        advisorSettings.setLanguage(SKAdvisorSettings.SKAdvisorLanguage.LANGUAGE_EN);
        advisorSettings.setAdvisorVoice("en");
        advisorSettings.setAdvisorType(SKAdvisorSettings.SKAdvisorType.TEXT_TO_SPEECH);
        SKRouteManager.getInstance().setAudioAdvisorSettings(advisorSettings);

        setContentView(R.layout.navigation_layout);

        setContentView(R.layout.navigation_layout);

        returnButton = (Button) findViewById(R.id.back_to_res_btn);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (skToolsNavigationInProgress) {
                    return;
                }
                textToSpeechEngine.shutdown();
                finish();
            }
        });

        waitForLocationTextBox = (TextView) findViewById(R.id.wait_for_location_message);
        nextAdviceTextBox = (TextView) findViewById(R.id.next_advice);

        mapHolder = (SKMapViewHolder) findViewById(R.id.map_surface_holder);
        mapHolder.setMapSurfaceListener(this);
        mapHolder.onResume();
    }

    @Override
    public void onSurfaceCreated(SKMapViewHolder skMapViewHolder) {
        Log.i("INFO: ", "onSurfaceCreated");
        // focus on starting point
        SKCoordinate startingPosition = MapUtils.getSKCoordinateFromGms(
                routeLatLngs.get(0));
        mapView = mapHolder.getMapSurfaceView();
        mapView.centerMapOnPosition(startingPosition);
        mapView.getMapSettings().setCompassPosition(new SKScreenPoint(0, 60));
        mapView.getMapSettings().setCompassShown(true);

        // set internationalization settings
        final SKMapInternationalizationSettings mapInternationalizationSettings =
                new SKMapInternationalizationSettings();
        mapInternationalizationSettings.setPrimaryLanguage(SKMaps.SKLanguage.LANGUAGE_EN);
        mapInternationalizationSettings.setFallbackLanguage(SKMaps.SKLanguage.LANGUAGE_DE);
        mapInternationalizationSettings.setFirstLabelOption(SKMapInternationalizationSettings
                .SKMapInternationalizationOption
                .MAP_INTERNATIONALIZATION_OPTION_INTL);
        mapInternationalizationSettings.setSecondLabelOption(SKMapInternationalizationSettings
                .SKMapInternationalizationOption
                .MAP_INTERNATIONALIZATION_OPTION_LOCAL);
        mapInternationalizationSettings.setShowBothLabels(true);
        mapView.getMapSettings()
                .setMapInternationalizationSettings(mapInternationalizationSettings);

        // set text to speech
        if (textToSpeechEngine == null) {
            textToSpeechEngine = new TextToSpeech(NavigationActivity.this,
                    new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status == TextToSpeech.SUCCESS) {
                                Toast.makeText(
                                        NavigationActivity.this,
                                        "Initialized Text-To-Speech",
                                        Toast.LENGTH_LONG)
                                        .show();
                                int result = textToSpeechEngine.setLanguage(Locale.ENGLISH);
                                if (result == TextToSpeech.LANG_MISSING_DATA || result ==
                                        TextToSpeech.LANG_NOT_SUPPORTED) {
                                    Toast.makeText(NavigationActivity.this,
                                            "This Language is not supported",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(NavigationActivity.this,
                                        "Failed to initialized Text-To-Speech",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        waitForLocationTextBox.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAllRoutesCompleted() {

        // get navigation settings object
        Log.i("INFO:", "inside all routes completed");
        SKNavigationSettings navigationSettings = new SKNavigationSettings();
        // set the desired navigation settings
        navigationSettings.setNavigationType(SKNavigationSettings.SKNavigationType.SIMULATION);
        navigationSettings.setPositionerVerticalAlignment(-0.25f);
        navigationSettings.setShowRealGPSPositions(true);
        navigationSettings.setEnableReferenceStreetNames(false);

        // get the navigation manager object
        navigationManager = SKNavigationManager.getInstance();
        navigationManager.setMapView(mapView);
        // set listener for navigation events
        navigationManager.setNavigationListener(this);

        // start navigation
        Log.i("INFO:", "start navigation");
        navigationManager.startNavigation(navigationSettings);
        skToolsNavigationInProgress = true;
        numberOfConsecutiveBadPositionReceivedDuringNavi = 0;
        gpsPositionsDelayChecker = new Handler();
        startPositionDelayChecker();
    }


    @Override
    public void onCurrentPositionUpdate(SKPosition currentPosition) {
        // Added for current location
        this.currentPositionTime = System.currentTimeMillis();

        if (currentPosition != null && !gotFirstCurrentLocation && mapView != null){
            Log.i("INFO:", "Got first current location");
            // never got current position
            this.currentPosition = currentPosition;
            gotFirstCurrentLocation = true;
            waitForLocationTextBox.setVisibility(View.INVISIBLE);
            positionAndCalcRoute(currentPosition);
        }
        else {
            this.currentPosition = currentPosition;
            if (this.currentPosition != null) {
                SKPositionerManager.getInstance().reportNewGPSPosition(this.currentPosition);
                if (skToolsNavigationInProgress) {
                    if (this.currentPosition.getHorizontalAccuracy() >= 150) {
                        numberOfConsecutiveBadPositionReceivedDuringNavi++;
                        if (numberOfConsecutiveBadPositionReceivedDuringNavi >= 3) {
                            numberOfConsecutiveBadPositionReceivedDuringNavi = 0;
                            onGPSSignalLost();
                        }
                    } else {
                        numberOfConsecutiveBadPositionReceivedDuringNavi = 0;
                        onGPSSignalRecovered();
                    }
                }
            }

        }
    }


    private void positionAndCalcRoute(SKPosition currentPosition) {
        Log.i("INFO: ", "position and calc route");
        mapView.centerMapOnPosition(currentPosition.getCoordinate());
        SKPositionerManager.getInstance().reportNewGPSPosition(currentPosition);

        // add a polyline by google coordinates
        List<SKPosition> pointsList = new ArrayList();
//        SKPolyline polyline = new SKPolyline();
//        List<SKCoordinate> nodes = new ArrayList<SKCoordinate>();

        for (com.google.android.gms.maps.model.LatLng google_point : routeLatLngs){
            pointsList.add(new SKPosition(google_point.longitude, google_point.latitude));
//            nodes.add(new SKCoordinate(google_point.longitude, google_point.latitude));
        }

        showDestinationOnMap(pointsList.get(pointsList.size()-1).getCoordinate());

//        // Add the google polyline
//        polyline.setNodes(nodes);
//        polyline.setColor(new float[]{0f, 0f, 1f, 1f});
//        polyline.setOutlineColor(new float[] { 0f, 0f, 1f, 1f });
//        polyline.setOutlineSize(4);
//        polyline.setIdentifier(12);
//        polyline.setOutlineDottedPixelsSolid(3);
//        polyline.setOutlineDottedPixelsSkip(3);
//        mapView.addPolyline(polyline);

        // set the route listener
        SKRouteSettings routeSettings = new SKRouteSettings();

        //set route mode
        routeSettings.setRouteMode(SKRouteSettings.SKRouteMode.PEDESTRIAN);
        routeSettings.setRouteExposed(true);
        routeSettings.setNoOfRoutes(1);

        // calculate the route
        Log.i("INFO:", "before calculating route");
        SKRouteManager.getInstance().setRouteListener(this);
        SKRouteManager.getInstance().calculateRouteWithPoints(pointsList, routeSettings);

        Log.i("INFO:", "after calculating route");

    }

    /**
     * Called when the gps signal was lost
     */
    private void onGPSSignalLost() {
        waitForLocationTextBox.setText(getString(R.string.gps_lost_text));
        waitForLocationTextBox.setVisibility(View.VISIBLE);
    }

    /**
     * Called when the gps signal was recovered after a loss
     */
    private void onGPSSignalRecovered() {
        waitForLocationTextBox.setVisibility(View.INVISIBLE);
        waitForLocationTextBox.setText(getString(R.string.wait_curr_location));
    }

    /**
     * runs the recursive gps signal checker
     */
    private void startPositionDelayChecker() {
        Log.i("INFO:", "startPositionDelayChecker");
        gpsPositionsDelayChecker.postDelayed(gpsPositionDelayCheckerRunnable, 5000);
    }

    /**
     * Checks if there is no new gps position and notifies if the signal was lost
     */
    private Runnable gpsPositionDelayCheckerRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i("INFO:", "gpsPositionDelayCheckerRunnable ");
            if (skToolsNavigationInProgress) {
                long lastPositionDelay = System.currentTimeMillis() - currentPositionTime;
                if (lastPositionDelay >= 5000) {
                    onGPSSignalLost();
                }
                startPositionDelayChecker();
            }
        }
    };


    @Override
    public void onDestinationReached() {
        nextAdviceTextBox.setText("Destination reached");
        returnButton.setVisibility(View.VISIBLE);
        navigationStop();
    }


    @Override
    public void onSignalNewAdviceWithInstruction(String instruction) {
        instruction = instruction.split("onto")[0].toUpperCase();
        SKLogging.writeLog("TTS", "onSignalNewAdviceWithInstruction " + instruction, Log.DEBUG);
        nextAdviceTextBox.setText(instruction);
        nextAdviceTextBox.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeechEngine.speak(instruction, TextToSpeech.QUEUE_ADD, null, null);
        }
        else{
            textToSpeechEngine.speak(instruction, TextToSpeech.QUEUE_ADD, null);

        }
    }

    private void navigationStop() {
        skToolsNavigationInProgress = false;
        if (textToSpeechEngine != null && !textToSpeechEngine.isSpeaking()) {
            textToSpeechEngine.stop();
        }
        gpsPositionsDelayChecker.removeCallbacks(gpsPositionDelayCheckerRunnable);
        SKRouteManager.getInstance().clearCurrentRoute();
        navigationManager.stopNavigation();
    }

    private void showDestinationOnMap(SKCoordinate destinationPoint){
        SKAnnotation annotation = new SKAnnotation(SKAnnotation
                .SK_ANNOTATION_TYPE_DESTINATION_FLAG);
        annotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
        annotation.setLocation(destinationPoint);
        mapView.addAnnotation(annotation,
                SKAnimationSettings.ANIMATION_NONE);
    }

    @Override
    public void onBackPressed() {
        Log.i("INFO:", "Back button pressed");
        if (skToolsNavigationInProgress) {
            showAlertDialog();
        }
        else {
            exitActivity();
        }
    }

    void exitActivity(){
        textToSpeechEngine.shutdown();
        finish();
    }

    void showAlertDialog(){

        /**
         * CREDIT: http://www.mkyong.com/android/android-alert-dialog-example/
         * http://developer.android.com/guide/topics/ui/dialogs.html
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(getString(R.string.exit_nav_title));

        alertDialogBuilder
                .setMessage(getString(R.string.exit_nav_q))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        NavigationActivity.super.onBackPressed();
                        navigationStop();
                        exitActivity();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    @Override
    public void onReRoutingStarted() {
        textToSpeechEngine.speak("Off route, stopping navigation!", TextToSpeech.QUEUE_ADD, null);
        Toast.makeText(NavigationActivity.this, "Off route, stopping navigation!",
                Toast.LENGTH_SHORT).show();
        navigationStop();
    }


    @Override
    public void onRouteCalculationCompleted(final SKRouteInfo routeInfo) {
        Log.i("INFO:", "route calc completed!");
        final List<SKRouteAdvice> advices = SKRouteManager.getInstance()
                .getAdviceList(routeInfo.getRouteID(),
                        SKMaps.SKDistanceUnitType.DISTANCE_UNIT_KILOMETER_METERS);
        skToolsRouteCalculated = true;
    }


    @Override
    public void onRouteCalculationFailed(SKRoutingErrorCode skRoutingErrorCode) {
        Log.i("Route CALC failed: ", String.format("%d", skRoutingErrorCode.getValue()));
        Toast.makeText(NavigationActivity.this, String.format(
                "Route calculation failed %d", skRoutingErrorCode.getValue()), Toast.LENGTH_SHORT)
                .show();
    }


    @Override
    public void onServerLikeRouteCalculationCompleted(SKRouteJsonAnswer skRouteJsonAnswer) {

    }


    @Override
    public void onFreeDriveUpdated(String s, String s1, String s2,
                                   SKNavigationState.SKStreetType skStreetType,
                                   double v, double v1) {

    }


    @Override
    public void onOnlineRouteComputationHanging(int i) {

    }


    @Override
    public void onViaPointReached(int i) {

    }

    @Override
    public void onVisualAdviceChanged(boolean b, boolean b1, SKNavigationState skNavigationState) {

    }


    @Override
    public void onTunnelEvent(boolean b) {

    }


    @Override
    public void onSignalNewAdviceWithAudioFiles(String[] strings, boolean b) {

    }


    @Override
    public void onSpeedExceededWithAudioFiles(String[] strings, boolean b) {

    }


    @Override
    public void onSpeedExceededWithInstruction(String s, boolean b) {

    }


    @Override
    public void onUpdateNavigationState(SKNavigationState skNavigationState) {

    }


    @Override
    public void onActionPan() {

    }

    @Override
    public void onActionZoom() {

    }

    @Override
    public void onMapRegionChanged(SKCoordinateRegion skCoordinateRegion) {

    }

    @Override
    public void onMapRegionChangeStarted(SKCoordinateRegion skCoordinateRegion) {

    }

    @Override
    public void onMapRegionChangeEnded(SKCoordinateRegion skCoordinateRegion) {

    }

    @Override
    public void onDoubleTap(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onSingleTap(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onRotateMap() {

    }

    @Override
    public void onLongPress(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onInternetConnectionNeeded() {

    }

    @Override
    public void onMapActionDown(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onMapActionUp(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onPOIClusterSelected(SKPOICluster skpoiCluster) {

    }

    @Override
    public void onMapPOISelected(SKMapPOI skMapPOI) {

    }

    @Override
    public void onAnnotationSelected(SKAnnotation skAnnotation) {

    }

    @Override
    public void onCustomPOISelected(SKMapCustomPOI skMapCustomPOI) {

    }

    @Override
    public void onCompassSelected() {

    }

    @Override
    public void onCurrentPositionSelected() {

    }

    @Override
    public void onObjectSelected(int i) {

    }

    @Override
    public void onInternationalisationCalled(int i) {

    }

    @Override
    public void onBoundingBoxImageRendered(int i) {

    }

    @Override
    public void onGLInitializationError(String s) {

    }

    @Override
    public void onScreenshotReady(Bitmap bitmap) {

    }

    @Override
    public void onNewVersionDetected(int i) {

    }

    @Override
    public void onMapVersionSet(int i) {

    }

    @Override
    public void onVersionFileDownloadTimeout() {

    }

    @Override
    public void onNoNewVersionDetected() {

    }
}
