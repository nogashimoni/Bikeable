package com.nnys.bikeable;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
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
import com.skobbler.ngx.map.SKPolyline;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class NavigationActivity extends AppCompatActivity implements SKPrepareMapTextureListener, SKMapUpdateListener, SKMapSurfaceListener, SKRouteListener, SKNavigationListener, SKCurrentPositionListener {

    /**
     * route inforamtion
     */
    BikeableRoute routeInfo;

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
        routeInfo = (BikeableRoute) getIntent().getSerializableExtra("BikeableRoute");

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

        // set up advisor
        final SKAdvisorSettings advisorSettings = initMapSettings.getAdvisorSettings();
        advisorSettings.setAdvisorConfigPath(mapResDirPath + "/Advisor");
        advisorSettings.setResourcePath(mapResDirPath + "/Advisor/Languages");
        advisorSettings.setLanguage(SKAdvisorSettings.SKAdvisorLanguage.LANGUAGE_EN);
        advisorSettings.setAdvisorVoice("en");
        advisorSettings.setAdvisorType(SKAdvisorSettings.SKAdvisorType.TEXT_TO_SPEECH);
        SKRouteManager.getInstance().setAudioAdvisorSettings(advisorSettings);

        setContentView(R.layout.navigation_layout);
        mapHolder = (SKMapViewHolder) findViewById(R.id.map_surface_holder);
        mapHolder.setMapSurfaceListener(this);
        mapHolder.onResume();
    }

    @Override
    public void onSurfaceCreated(SKMapViewHolder skMapViewHolder) {
        Log.i("INFO: ", "onSurfaceCreated");
        // focus on starting point
        SKCoordinate startingPosition = MapUtils.getSKCoordinateFromModel(
                routeInfo.getRouteLatLngArr().get(0));
        mapView = mapHolder.getMapSurfaceView();
        mapView.centerMapOnPosition(startingPosition);
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

        // set tts
        if (textToSpeechEngine == null) {
            Toast.makeText(NavigationActivity.this, "Initializing TTS engine", Toast.LENGTH_LONG).show();
            textToSpeechEngine = new TextToSpeech(NavigationActivity.this,
                    new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status == TextToSpeech.SUCCESS) {
                                Toast.makeText(
                                        NavigationActivity.this,
                                        "TTS success",
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
                                Toast.makeText(NavigationActivity.this,"TTS not initialized!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
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


        Log.i("INFO:", "start navigation");
        navigationManager.startNavigation(navigationSettings);
        skToolsNavigationInProgress = true;
        numberOfConsecutiveBadPositionReceivedDuringNavi = 0;
//        gpsPositionsDelayChecker = new Handler();
//        startPositionDelayChecker();
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
            positionAndCalcRoute(currentPosition);
        }
        else {
            this.currentPosition = currentPosition;
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

    private void positionAndCalcRoute(SKPosition currentPosition) {
        Log.i("INFO: ", "position and calc route");
        mapView.centerMapOnPosition(currentPosition.getCoordinate());
        SKPositionerManager.getInstance().reportNewGPSPosition(currentPosition);
        GeoApiContext context = new GeoApiContext().setApiKey(getString(R.string.api_key_server));

        // add a polyline by google coordinates
        List pointsList = new ArrayList();
        SKPolyline polyline = new SKPolyline();
        List<SKCoordinate> nodes = new ArrayList<SKCoordinate>();

        for (LatLng google_point : routeInfo.getRouteLatLngArr()){
            pointsList.add(new SKPosition(google_point.lng, google_point.lat));
            nodes.add(new SKCoordinate(google_point.lng, google_point.lat));
        }
        // Add the google polyline
        polyline.setNodes(nodes);
        polyline.setColor(new float[]{0f, 0f, 1f, 1f});
        polyline.setOutlineColor(new float[] { 0f, 0f, 1f, 1f });
        polyline.setOutlineSize(4);
        polyline.setIdentifier(12);
        polyline.setOutlineDottedPixelsSolid(3);
        polyline.setOutlineDottedPixelsSkip(3);
        mapView.addPolyline(polyline);

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
//        navigationManager.showSearchingForGPSPanel();
        Toast.makeText(NavigationActivity.this, "GPS signal lost", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the gps signal was recovered after a loss
     */
    private void onGPSSignalRecovered() {
//        navigationManager.hideSearchingForGPSPanel();
//        Toast.makeText(MainActivity.this, "GPS signal recovered", Toast.LENGTH_SHORT).show();

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

    @Override
    public void onDestinationReached() {
        navigationStop();
    }

    private void navigationStop() {
        skToolsNavigationInProgress = false;
        if (textToSpeechEngine != null && !textToSpeechEngine.isSpeaking()) {
            textToSpeechEngine.stop();
        }
        navigationManager.stopNavigation();
    }

    @Override
    public void onSignalNewAdviceWithInstruction(String instruction) {
        instruction = instruction.split("onto")[0];
        SKLogging.writeLog("TTS", "onSignalNewAdviceWithInstruction " + instruction, Log.DEBUG);
        textToSpeechEngine.speak(instruction, TextToSpeech.QUEUE_ADD, null);
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
    public void onReRoutingStarted() {
        Toast.makeText(NavigationActivity.this, "not rerouting! stopping navigation!", Toast.LENGTH_SHORT).show();
        navigationStop();
    }
    @Override
    public void onFreeDriveUpdated(String s, String s1, String s2, SKNavigationState.SKStreetType skStreetType, double v, double v1) {

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
    public void onRouteCalculationCompleted(final SKRouteInfo routeInfo) {
        Toast.makeText(NavigationActivity.this,"Calculation completed!", Toast.LENGTH_SHORT).show();

        Log.i("INFO:", "route calc completed!");
        final List<SKRouteAdvice> advices = SKRouteManager.getInstance()
                .getAdviceList(routeInfo.getRouteID(),
                        SKMaps.SKDistanceUnitType.DISTANCE_UNIT_KILOMETER_METERS);
        if (advices != null) {
            for (SKRouteAdvice advice : advices) {
                Log.i("INFO_ADVICE", " Route advice is " + advice.toString());
            }
        }
        skToolsRouteCalculated = true; // TODO: is this needed?

    }

    @Override
    public void onRouteCalculationFailed(SKRoutingErrorCode skRoutingErrorCode) {
        Log.i("INFO:", "route calc Failed!!");
        Log.i("CALC failed: " , String.format("%d", skRoutingErrorCode.getValue()));
        Toast.makeText(NavigationActivity.this, String.format(
                "Calculation failed %d", skRoutingErrorCode.getValue()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServerLikeRouteCalculationCompleted(SKRouteJsonAnswer skRouteJsonAnswer) {

    }

    @Override
    public void onOnlineRouteComputationHanging(int i) {

    }
}
