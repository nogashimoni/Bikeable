package com.nnys.bikeable;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.GeoApiContext;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.maps.GeocodingApi;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.GeocodingResult;
import com.jjoe64.graphview.GraphView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

////////////////////dynamodb imports//////////
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;


public class CentralActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final LatLngBounds BOUNDS_GREATER_TEL_AVIV = new LatLngBounds(
            new LatLng(32.009575, 34.662469), new LatLng(32.240376, 35.011864));

    private static final LatLng TAU_LATLNG = new LatLng(32.113496, 34.804388);

    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;

    protected GoogleApiClient mGoogleApiClient;
    protected GeoApiContext geoApiContext;
    protected DirectionsManager directionsManager = null;

    private AllRoutes allRoutes;

    private FloatingActionButton clearBtn, showGraphBtn, bikePathButton, singleBikePathButton, startNavButton;
    private Button historyBtn;
    private ImageButton searchBtn;

    private ArrayList<com.google.maps.model.LatLng> points = new ArrayList<>();
    private GoogleMap mMap;
    private Marker tempMarker;
    private Marker currInfoMarker;
    private ClearableAutoCompleteTextView to, from;
    private PlaceAutocompleteAdapter fromAdapter;
    private LinearLayout searchLayout;

    private PathElevationGraphDrawer graphDrawer;
    private GraphView graph;

    private Location mCurrentLocation = null;
    private String mLastUpdateTime;
    private LocationRequest mLocationRequest;
    private boolean isCurrentLocationAlreadyUpdated;
    private boolean mDoAskToUseLocation = true;
    private CustomAutoCompletePrediction currentLocationPrediction;

    TextView pathDurationTextView;
    TextView pathPercTextView;
    TextView pathDistanceTextView;
    TextView rankingTextView;

    MenuItem menuSearch;
    MenuItem cbBikePathPrefered, cbAvoidUphills;
    MenuItem cbAllBikePath, cbOverlapBikePath, cbUphillsSections, cbAllTelOFun, cbNearestTelOFun;


//    private boolean isShowBikeRouteMatchesChecked = false;
//    private boolean isShowCloseTelOFunStationsChecked = false;
//    private boolean isShowUphillSections;
//    private boolean isAvoidUphillsChecked = true;
//    private boolean isPreferBikePathChecked = true;

    private SearchHistoryCollector searchHistoryCollector;
    private SharedPreferences sharedPreferences;
    private UserPreferences userPreferences;
    private LayerPreferences layerPreferences;
    private boolean isSlidingPanelEnabled = false;

    private DynamoDBMapper mapper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.central_activity_layout);

        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        geoApiContext = new GeoApiContext().setApiKey(getString(R.string.api_key_server));

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

        pathDurationTextView = (TextView)findViewById(R.id.path_duration);
        pathPercTextView = (TextView)findViewById(R.id.bike_path_perc);
        pathDistanceTextView = (TextView)findViewById(R.id.path_distance);
        rankingTextView = (TextView)findViewById(R.id.path_rank);

        searchHistoryCollector = new SearchHistoryCollector(CentralActivity.this, geoApiContext);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        userPreferences = new UserPreferences(sharedPreferences);
        layerPreferences = new LayerPreferences(sharedPreferences);

        userPreferences.loadUserPreferences();
        layerPreferences.loadLayerPreferences();

        currentLocationPrediction = new CustomAutoCompletePrediction(
                getResources().getString(R.string.curr_location_primary_text),
                getResources().getString(R.string.curr_location_secondary_text));

        from = (ClearableAutoCompleteTextView) findViewById(R.id.from);
        from.setImgClearButtonColor(ContextCompat.getColor(this, R.color.colorPrimary));
        to = (ClearableAutoCompleteTextView) findViewById(R.id.to);
        to.setImgClearButtonColor(ContextCompat.getColor(this, R.color.colorPrimary));
        to.setSearchHistoryCollector(searchHistoryCollector);
        from.setAdapter(new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_TEL_AVIV,
                null));
        fromAdapter  = (PlaceAutocompleteAdapter)from.getAdapter();
        fromAdapter.addFixedResult(currentLocationPrediction);
        to.setAdapter(new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_TEL_AVIV,
                null));
        from.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                from.setPrediction((AutocompletePrediction) parent.getItemAtPosition(position), false);
                if (isSearchFromCurrentLocation()) {
                    LatLng currentLocationLatLng = null;
                    if (mCurrentLocation != null) {
                        currentLocationLatLng = MapUtils.getGMSFromLocation(mCurrentLocation);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Current Location Unavailable", Toast.LENGTH_SHORT).show();
                    }
                    directionsManager.setFromCurrLocation(currentLocationLatLng, (CustomAutoCompletePrediction) from.getPrediction());
                } else {
                    directionsManager.setNewMarkerByPlacePrediction(true, from.getPrediction());
                }
                updateMapToNewMArkerState();
            }
        });

        to.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                to.setPrediction((AutocompletePrediction) parent.getItemAtPosition(position), false);
                if (tempMarker != null)
                    tempMarker.remove();
                directionsManager.setNewMarkerByPlacePrediction(false, to.getPrediction());
                updateMapToNewMArkerState();
            }
        });
        from.setOnClearExtraListener(new ClearableAutoCompleteTextView.OnClearExtraListener() {
            @Override
            public void onClearExtra() {
                directionsManager.clearNewMarker(true);
            }
        });
        to.setOnClearExtraListener(new ClearableAutoCompleteTextView.OnClearExtraListener() {
            @Override
            public void onClearExtra() {
                directionsManager.clearNewMarker(false);
            }
        });

        allRoutes = new AllRoutes();
        graph = (GraphView) findViewById(R.id.altitude_graph);


        final MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        directionsManager = new DirectionsManager(geoApiContext, mMap);

        searchLayout = (LinearLayout) findViewById(R.id.search_layout);
        searchBtn = (ImageButton) findViewById(R.id.res_button);
        startNavButton = (FloatingActionButton ) findViewById(R.id.start_nav_button);
        searchBtn.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        searchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (from.getPrediction() == null || to.getPrediction() == null) {
                    return;
                }
                if (isSearchFromCurrentLocation()){
                    if (mCurrentLocation == null) {
                        Toast.makeText(getApplicationContext(), "Current Location Unavailable", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {
                        directionsManager.setFromLatLngNew(MapUtils.getGMSFromLocation(mCurrentLocation));
                    }
                }

                startNavButton.setVisibility(View.GONE);

                // hide keyboard on search
                hideKeyboard();
//                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                in.hideSoftInputFromWindow(v.getWindowToken(), 0);

                //put in the search button onClick:
                BackgroundTask task = new BackgroundTask(CentralActivity.this);
                task.execute();

                enableSlidingPanel();

            }
        });

        startNavButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<LatLng> selectedRouteLatLngs = MapUtils.getLstGmsLatLngFromModel(
                        allRoutes.getSelectedRoute().getRouteLatLngs());
                Log.i("INFO:", String.format("route before starting activity!!! %d", selectedRouteLatLngs.size()));

                if (selectedRouteLatLngs != null) {
                    Intent navIntent = new Intent(CentralActivity.this, NavigationActivity.class);
                    navIntent.putExtra("routeLatLngs", selectedRouteLatLngs);
                    startActivity(navIntent);
                }
            }
        });

        disableSlidingPanel();
    }

    private void setCurrentLocationAsDefault() {
        if (mCurrentLocation == null) {
            Toast.makeText(getApplicationContext(), "Current Location Unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        if (from.getPrediction() != null && !isSearchFromCurrentLocation()){
            return;
        }
        from.setPrediction(currentLocationPrediction, false);
        from.setText(currentLocationPrediction.getFullText(null), false);
        directionsManager.setFromCurrLocation(MapUtils.getGMSFromLocation(mCurrentLocation), (CustomAutoCompletePrediction) from.getPrediction());
    }

    private boolean isSearchFromCurrentLocation() {
        if (from.getPrediction() == null) {
            return false;
        }
        return from.getPrediction()
                .getSecondaryText(new StyleSpan(Typeface.BOLD))
                .toString()
                .equals(getResources().getString(R.string.curr_location_secondary_text));
    }

    private void hideSearchView() {
        searchLayout.setVisibility(View.GONE);
        if (menuSearch != null){
            menuSearch.setVisible(true);
            menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }


    }

    private void showSearchView() {
        searchLayout.setVisibility(View.VISIBLE);
        if (menuSearch != null){
            menuSearch.setVisible(false);
        }

    }

    private void disableSlidingPanel() {
        SlidingUpPanelLayout slidingUpLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingUpLayout.setPanelHeight(0);
        slidingUpLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        slidingUpLayout.setPanelHeight(0);
        isSlidingPanelEnabled = false;
    }

    private void enableSlidingPanel() {
        SlidingUpPanelLayout slidingUpLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingUpLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        slidingUpLayout.setPanelHeight(78);
        isSlidingPanelEnabled = true;
    }

    private void updateInfoTable() {
        BikeableRoute currentRoute = allRoutes.getSelectedRoute();
        if ( currentRoute == null ) {
            clearInfoTable();
            return;
        }

        clearInfoTable();

        pathDurationTextView.setText(String.format("%s", currentRoute.getDurationString()));
        pathPercTextView.setText(String.format("%d", (int) ((currentRoute.getBikePathPercentage() * 100) + 0.5)) + "%");
        pathDistanceTextView.setText(String.format("%s", currentRoute.getDistanceString()));
        StringBuilder rankBy = new StringBuilder("");
        if (userPreferences.doesUserAvoidUphills()){
            rankBy.append("(Uphills");
            if (userPreferences.doesUserPrefereBikingRoutes()){
                rankBy.append(" + Bike paths)");
            }
            else {
                rankBy.append(")");
            }
        }
        else if (userPreferences.doesUserPrefereBikingRoutes()){
            rankBy.append("(Bike paths)");
        }
        else{
            rankBy.append("(Fastest)");
        }
        rankingTextView.setText(String.format(" %s: %d", rankBy.toString(), allRoutes.getSelectedRouteRank() )); //(String.format("%.2f", currentRoute.getAverageUphillDegree()) + "°");
    }

    private void clearInfoTable() {
        pathDurationTextView.setText("");
        pathPercTextView.setText("");
        pathDistanceTextView.setText("");
        rankingTextView.setText("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_central, menu);

        menuSearch = menu.findItem(R.id.action_search);
        menuSearch.setVisible(false);

        cbBikePathPrefered = menu.findItem(R.id.bike_paths);
        cbAvoidUphills = menu.findItem(R.id.elevations);
        cbBikePathPrefered.setChecked(userPreferences.doesUserPrefereBikingRoutes());
        cbAvoidUphills.setChecked(userPreferences.doesUserAvoidUphills());

        cbUphillsSections = menu.findItem(R.id.uphill_sections);
        cbAllBikePath = menu.findItem(R.id.iria_bike_path_cb);
        cbOverlapBikePath = menu.findItem(R.id.iria_bike_path_mathches);
        cbAllTelOFun = menu.findItem(R.id.iria_telOFun_cb);
        cbNearestTelOFun = menu.findItem(R.id.iria_telOFun_matches);

        cbUphillsSections.setChecked(layerPreferences.isShowUphills());
        cbAllBikePath.setChecked(layerPreferences.isShowAllBikePaths());
        cbOverlapBikePath.setChecked(layerPreferences.isShowOverlapBikePaths());
        cbAllTelOFun.setChecked(layerPreferences.isShowAllTelOFun());
        cbNearestTelOFun.setChecked(layerPreferences.isShowNearTelOFun());

        Drawable drawable = menuSearch.getIcon();
        if (drawable != null) {
//            drawable.mutate();
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY );
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.elevations: // avoid steep uphills
                if (!item.isChecked()){
                    item.setChecked(true);
                    userPreferences.setUserAvoidsUphills(true);
                }

                else{
                    item.setChecked(false);
                    userPreferences.setUserAvoidsUphills(false);
                }
                userPreferences.saveUserPreferences();
                return true;

            case R.id.bike_paths: // prefer biking routes
                if (!item.isChecked()){
                    item.setChecked(true);
                    userPreferences.setUserPreferesBikingRoutes(true);
                }

                else{
                    item.setChecked(false);
                    userPreferences.setUserPreferesBikingRoutes(false);
                }
                userPreferences.saveUserPreferences();
                return true;

            case R.id.iria_bike_path_cb: // tel aviv bike paths
                if (!item.isChecked()){
                    if (!IriaData.isDataReceived){
                        Toast.makeText(
                                CentralActivity.this,
                                "Failed to get Tel-Aviv Municipality Data",
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    item.setChecked(true);
                    IriaData.addBikePathToMap(mMap);
                    IriaData.showBikePathOnMap();
                    layerPreferences.setShowAllBikePaths(true);
                }
                else{
                    item.setChecked(false);
                    layerPreferences.setShowAllBikePaths(false);
                    if (IriaData.isDataReceived){
                        IriaData.removeBikePathFromMap();
                    }
                }
                layerPreferences.saveLayerPreferences();
                return true;

            case R.id.iria_bike_path_mathches: // overlapping bike paths
                if (!item.isChecked()){
                    if (!IriaData.isDataReceived){
                        Toast.makeText(
                                CentralActivity.this,
                                "Failed to get Tel-Aviv Municipality Data",
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    item.setChecked(true);
                    layerPreferences.setShowOverlapBikePaths(true);
                    showBikePathMatchesOnMap();
                } else {
                    item.setChecked(false);
                    layerPreferences.setShowOverlapBikePaths(false);
                    if (IriaData.isDataReceived){
                        removeBikePathMatchesFromMap();
                    }
                }
                layerPreferences.saveLayerPreferences();
                return true;

            case R.id.iria_telOFun_cb: // tel o fun stations
                if (!item.isChecked()){
                    if (!IriaData.isDataReceived){
                        Toast.makeText(
                                CentralActivity.this,
                                "Failed to get Tel-Aviv Municipality Data",
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    item.setChecked(true);
                    layerPreferences.setShowAllTelOFun(true);
                    IriaData.addTelOFunToMap(mMap);
                    IriaData.showTelOFunOnMap();
                }
                else{
                    item.setChecked(false);
                    layerPreferences.setShowAllTelOFun(false);
                    if (IriaData.isDataReceived){
                        IriaData.removeTelOFunFromMap();
                    }
                }
                layerPreferences.saveLayerPreferences();
                return true;

            case R.id.iria_telOFun_matches: // tel o fun near start/end point
                if (!item.isChecked()){
                    if (!IriaData.isDataReceived){
                        Toast.makeText(
                                CentralActivity.this,
                                "Failed to get Tel-Aviv Municipality Data",
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    item.setChecked(true);
                    layerPreferences.setShowNearTelOFun(true);
                    allRoutes.showTelOFunSourceMatchesOnMap();
                    allRoutes.showTelOFunDestinationMatchesOnMap();
                }
                else{
                    item.setChecked(false);
                    layerPreferences.setShowNearTelOFun(false);
                    if (IriaData.isDataReceived){
                        allRoutes.hideTelOFunSourceMatchesOnMap();
                        allRoutes.hideTelOFunDestinationMatchesOnMap();
                    }
                }
                layerPreferences.saveLayerPreferences();
                return true;

            case R.id.uphill_sections: // significant uphills
                if (!item.isChecked()){
                    item.setChecked(true);
                    layerPreferences.setShowUphills(true);
                    allRoutes.showUphillSections(mMap);
                }
                else{
                    item.setChecked(false);
                    layerPreferences.setShowUphills(false);
                    allRoutes.hideUphillSections();
                }
                layerPreferences.saveLayerPreferences();
                return true;

            case R.id.action_search: // search button
                showSearchView();
                return true;

            case R.id.action_feedback: // feedback button
                Intent Email = new Intent(Intent.ACTION_SEND);
                Email.setType("message/rfc822");
                Email.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.feedback_email) });
                Email.putExtra(Intent.EXTRA_SUBJECT, "Bikeable Feedback");
                Email.putExtra(Intent.EXTRA_TEXT, "Dear Bikeable Team," + "");
                startActivity(Intent.createChooser(Email, "Send Feedback:"));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void removeBikePathMatchesFromMap() {
        if (allRoutes.bikeableRoutes.isEmpty()) { // no routes on map
            return;
        }
        else {
            for (BikeableRoute route : allRoutes.bikeableRoutes) {
                route.hideBikePathFromMap();;
            }
        }
    }

    private void showBikePathMatchesOnMap() {
        if (allRoutes.bikeableRoutes.isEmpty()) { // no routes on map
            return;
        }
        else {
            for (BikeableRoute route : allRoutes.bikeableRoutes) {
                route.showBikePathOnMap();
            }
        }
    }

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // MapWrapperLayout initialization
        final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout)findViewById(R.id.map_relative_layout);
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));

        if (directionsManager != null && directionsManager.getMap() == null) {
            directionsManager.setMap(mMap);
        }

        mMap.setMyLocationEnabled(true);

        LatLng placeToFocusOn;
        if ( mCurrentLocation == null ) {
            Log.i("INFO:", "set focus on tlv");
            placeToFocusOn = TAU_LATLNG;            // focus map on tau
        } else {
            Log.i("INFO:", "set focus on current location");
            placeToFocusOn = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
//        mMap.addMarker(new MarkerOptions()
//                        .title("current location (or tau)")
//                        .position(placeToFocusOn)
//        );
//

        if (layerPreferences != null){
            Log.i("INFO:", "layerPreferences not null");
            if (layerPreferences.isShowAllBikePaths()){
                IriaData.addBikePathToMap(mMap);
                IriaData.showBikePathOnMap();
            }
            if (layerPreferences.isShowAllTelOFun()){
                IriaData.addTelOFunToMap(mMap);
                IriaData.showTelOFunOnMap();
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(placeToFocusOn));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15f));

        mMap.setOnMapClickListener((new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng clickLatLng) {
                hideKeyboard();
                Log.i("inside listener begin", "inside listener begin2");
                if (tempMarker != null)
                    tempMarker.remove();

                if (!allRoutes.bikeableRoutes.isEmpty()) {
                    boolean isRouteClicked = MapUtils.selectClickedRoute(allRoutes, clickLatLng);

                    if (isRouteClicked && allRoutes.getSelectedRouteIndex() >= 0) {
                        graphDrawer.setSelectedSeriesAndColorIt(allRoutes.getSelectedRouteIndex());
                        updateInfoTable();
                    }
                }
            }
        }
        ));

        MarkersInfoWindowAdapter markersInfoWindowAdapter = new MarkersInfoWindowAdapter(getLayoutInflater(), mapWrapperLayout);

        markersInfoWindowAdapter.getInfoOrigBtn().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_DOWN) {
                    return false;
                }
                updateMarkerButtonClick(true);
                return false;
            }
        });

        markersInfoWindowAdapter.getInfoDstBtn().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_DOWN) {
                    return false;
                }
                updateMarkerButtonClick(false);
                return false;
            }
        });


        mMap.setInfoWindowAdapter(markersInfoWindowAdapter);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

               @Override
               public void onMapLongClick(LatLng markerLatLng) {
                   hideKeyboard();
                   if (isSlidingPanelEnabled) {
                       Log.i("INFO:", "isSlidingPanelEnabled");
                       enableSlidingPanel();
                   }
                   if (tempMarker != null) {
                       tempMarker.remove();
                   }
                   tempMarker = mMap.addMarker(new MarkerOptions()
                           .position(markerLatLng));
                   currInfoMarker = tempMarker;
                   tempMarker.setTitle("Temp Marker");
                   tempMarker.showInfoWindow();

               }
           }
        );

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                currInfoMarker = marker;
                if (marker.getTitle().equals("TelOFun")) {
                    try {
                        //IriaData.updateTelOFunBikesAvailability(marker);
                        Log.i("Info", "before calling to update");
                        IriaData.updateTelOFunBikesAvailabilityWithDynamoDB(marker, mapper);
                        marker.showInfoWindow();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "TelOFun data Unavailable", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    marker.showInfoWindow();
                }
                return true;
            }
        });
    }


    @Override
    public void onConnected(Bundle connectionHint) {
//        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                mGoogleApiClient);
//        //mLastLocation.getAltitude();
//        if (mLastLocation != null) {
//            Log.i("INFO", String.format("Current loction lat: %f",mLastLocation.getLatitude()));
//            Log.i("INFO", String.format("Current location lang %f",mLastLocation.getLongitude()));
//        } else {
//            Log.i("INFO", "current position is NULL");
//        }

            createLocationRequest();

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // if sdk is marshmallow
                if (ContextCompat.checkSelfPermission(CentralActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // permission is not granted
                    if (ActivityCompat.shouldShowRequestPermissionRationale(CentralActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // if permissions explanation is needed
                        Toast.makeText(getApplicationContext(),
                                "GPS permission allows us to access location data." +
                                " Please allow in App Settings for additional " +
                                "functionality.",Toast.LENGTH_LONG).show();
                    } else {
                        ActivityCompat.requestPermissions(CentralActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_CODE_LOCATION);
                    }
                    return; // permission is not yet granted
                }
            }

            startLocationUpdates();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(getApplicationContext(),"Permission Denied," +
                            " You cannot access location data.",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.i("INFO", String.format("Current loction lat: %f", mCurrentLocation.getLatitude()));
        Log.i("INFO", String.format("Current location lang %f", mCurrentLocation.getLongitude()));
        updateUI();
        if (!isCurrentLocationAlreadyUpdated) {
            setCurrentLocationAsDefault();
        }
        isCurrentLocationAlreadyUpdated = true;

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (mDoAskToUseLocation) {
        /* ask to turn on location
            CREDIT: http://stackoverflow.com/questions/29801368/how-to-show-enable-location-dialog-like-google-maps
         */
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        CentralActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });

            mDoAskToUseLocation = false;
        }
    }

    public void updateUI() {


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
//        moveTaskToBack(true);
        showAlertDialog();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
//    }


    void showAlertDialog(){

        /**
         * CREDIT: http://www.mkyong.com/android/android-alert-dialog-example/
         * http://developer.android.com/guide/topics/ui/dialogs.html
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(getString(R.string.exit_central_title));

        alertDialogBuilder
                .setMessage(getString(R.string.exit_central_q))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        CentralActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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




    // put as a private class inside the activity class
    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        CentralActivity activity;
        ProgressDialog ringProgressDialog;

        public BackgroundTask(CentralActivity activity) {
            ringProgressDialog = new ProgressDialog(activity);//TODO: For Noa.
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
//            disableSlidingPanel();
            ringProgressDialog.setTitle("Calculating routes");
            ringProgressDialog.setMessage("Please wait...");
            ringProgressDialog.setIndeterminate(true);
            ringProgressDialog.setCancelable(false);
            ringProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            ringProgressDialog.show();

        }

        @Override
        protected void onPostExecute(Void result) {

            // put here the search code
            directionsManager.getDirections();
            directionsManager.addCurrentSearchTargetToHistory(searchHistoryCollector);
            allRoutes.updateBikeableRoutesAndMap(directionsManager.getCalculatedRoutes(), mMap, userPreferences);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(directionsManager.getDirectionBounds(), getResources()
                    .getInteger(R.integer.bound_padding)));
            //allRoutes.chooseTelOFunMatchesToSourceAndDestination (mMap, directionsManager);
            try {
                allRoutes.calculateClosestTelOFunStationsData(mMap, directionsManager);
            } catch (IOException e) {
                e.printStackTrace();
            }

            graphDrawer = new PathElevationGraphDrawer(graph);

            for (int i = 0; i < allRoutes.bikeableRoutes.size(); i++ ) {
                BikeableRoute bikeableRoute = allRoutes.bikeableRoutes.get(i);
                ElevationResult[] results = bikeableRoute.elevationQuerier
                        .getElevationSamples(bikeableRoute.numOfElevationSamples);
                graphDrawer.addSeries(results, i);
            }
            if (allRoutes.bikeableRoutes.size() == 0){
                Toast.makeText(getApplicationContext(), "Origin and destination too close.", Toast.LENGTH_SHORT).show();
            }

            graphDrawer.setSelectedSeriesAndColorIt(allRoutes.getBestRouteIndex());

            GraphToMapConnector graphToMapConnector = new GraphToMapConnector(graphDrawer, mMap);
            graphToMapConnector.connect();

            if ( layerPreferences.isShowOverlapBikePaths()){
                showBikePathMatchesOnMap();
            }
            if (layerPreferences.isShowNearTelOFun()){
                allRoutes.showTelOFunDestinationMatchesOnMap();
                allRoutes.showTelOFunSourceMatchesOnMap();
            }
            if (layerPreferences.isShowUphills()) {
                Log.i("Info:", "isShowUphillSections");
                allRoutes.showUphillSections(mMap);
            }
            updateInfoTable();
            hideSearchView();
//            enableSlidingPanel();


            if (isSearchFromCurrentLocation()) {
                startNavButton.setVisibility(View.VISIBLE);
            }

            if (ringProgressDialog.isShowing()) {
                ringProgressDialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    private void updateMarkerButtonClick(boolean isFrom){
        String primaryText;
        String secondaryText;
        String placeId = null;

        if (currInfoMarker.getTitle().equals("TelOFun")){
            int stationId = Integer.parseInt(currInfoMarker.getSnippet());
            String stationName = IriaData.getTelOfanStationsDict().get(stationId).getStationName();
            primaryText = String.format("TelOFun (%s): %s", currInfoMarker.getSnippet(), stationName);
            secondaryText = "";
        }
        else {
            try {
                GeocodingResult[] results = GeocodingApi.newRequest(geoApiContext)
                        .latlng(MapUtils.getModelLatLngFromGms(currInfoMarker.getPosition())).await();
                primaryText = results[0].formattedAddress;
                secondaryText = "";
                placeId = results[0].placeId;
            } catch (Exception e) {
                primaryText = "Custom Origin";
                secondaryText = String.format("(%f ,%f)", currInfoMarker.getPosition()
                        .latitude, currInfoMarker.getPosition().longitude);
                e.printStackTrace();
            }
        }
        CustomAutoCompletePrediction newPrediction =
                new CustomAutoCompletePrediction(primaryText, secondaryText, placeId);
        if (isFrom){
            from.setPrediction(newPrediction, true);
        }
        else{
            to.setPrediction(newPrediction, true);
        }
        directionsManager.setNewMarkerByCustomPrediction(isFrom, currInfoMarker.getPosition(), newPrediction);
        updateMapToNewMArkerState();
    }

    private void updateMapToNewMArkerState() {
        Log.i("Info:", "updateMapToNewMArkerState");
        startNavButton.setVisibility(View.GONE);
        showSearchView();

        allRoutes.removeCurrentRoutes();
        disableSlidingPanel();

        hideKeyboard();

        if (tempMarker != null) {
            tempMarker.remove();
        }
    }

    private void hideKeyboard(){
        // hide keyboard on search
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (CentralActivity.this.getCurrentFocus() != null) {
            in.hideSoftInputFromWindow(CentralActivity.this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }
}
