package com.nnys.bikeable;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.GeoApiContext;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.maps.PlacesApi;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.PlaceDetails;
import com.jjoe64.graphview.GraphView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class CentralActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final LatLngBounds BOUNDS_GREATER_TEL_AVIV = new LatLngBounds(
            new LatLng(32.009575, 34.662469), new LatLng(32.240376, 35.011864));

    private static final LatLng TAU_LATLNG = new LatLng(32.113496, 34.804388);

    protected GoogleApiClient mGoogleApiClient;
    protected GeoApiContext context;
    protected DirectionsManager directionsManager = null;

    private AllRoutes allRoutes;

    private FloatingActionButton clearBtn, showGraphBtn, bikePathButton, singleBikePathButton,
            startNavButton;
    private ImageButton searchBtn;

    private ArrayList<com.google.maps.model.LatLng> points = new ArrayList<>();
    private GoogleMap mMap;
    private Marker tempMarker;
    private ClearableAutoCompleteTextView to, from;
    private LinearLayout searchLayout;
    private LinearLayout markerOptsLayout;


    private PathElevationGraphDrawer graphDrawer;
    private GraphView graph;

    private Location mCurrentLocation = null;
    private String mLastUpdateTime;
    private LocationRequest mLocationRequest;

    boolean isSearchFromCustom, isSearchToCustom;
    boolean isSearchFromCurrentLocation;

    TextView pathDurationTextView;
    TextView pathPercTextView;
    TextView pathDistanceTextView;
    TextView pathUphillAverageTextView;

    MenuItem menuSearch;

    private boolean isShowBikeRouteMatchesChecked = false;
    private boolean isShowCloseTelOFunStationsChecked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.central_activity_layout);

        disableSlidingPanel();

        pathDurationTextView = (TextView)findViewById(R.id.path_duration);
        pathPercTextView = (TextView)findViewById(R.id.bike_path_perc);
        pathDistanceTextView = (TextView)findViewById(R.id.path_distance);
        pathUphillAverageTextView = (TextView)findViewById(R.id.path_difficulty);

        from = (ClearableAutoCompleteTextView) findViewById(R.id.from);
        from.setImgClearButtonColor(ContextCompat.getColor(this, R.color.colorPrimary));
        to = (ClearableAutoCompleteTextView) findViewById(R.id.to);
        to.setImgClearButtonColor(ContextCompat.getColor(this, R.color.colorPrimary));
        from.setAdapter(new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_TEL_AVIV,
                null));
        PlaceAutocompleteAdapter fromAdapter  = (PlaceAutocompleteAdapter)from.getAdapter();
        fromAdapter.addFixedResult(new CustomAutoCompletePrediction(
                getResources().getString(R.string.curr_location_primary_text),
                getResources().getString(R.string.curr_location_secondary_text)));
        to.setAdapter(new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_TEL_AVIV,
                null));
        from.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                markerOptsLayout.setVisibility(View.GONE);
                from.setPrediction((AutocompletePrediction) parent.getItemAtPosition(position), false);
                if (isSearchFromCurrentLocation()) {
                    directionsManager.setNewMarkerByCustomPrediction(true, MapUtils.getGMSFromLocation(mCurrentLocation), (CustomAutoCompletePrediction) from.getPrediction());
                    isSearchFromCustom = true;
                } else {
                    directionsManager.setNewMarkerByPlacePrediction(true, from.getPrediction());
                    Log.i("INFO from:", directionsManager.getFromMarkerNew().getTitle());
                    isSearchFromCustom = false;
                }
                if (tempMarker != null)
                    tempMarker.remove();
            }
        });

        to.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                to.setPrediction((AutocompletePrediction) parent.getItemAtPosition(position), false);
                if (tempMarker != null)
                    tempMarker.remove();
                markerOptsLayout.setVisibility(View.GONE);
                directionsManager.setNewMarkerByPlacePrediction(false, to.getPrediction());
                Log.i("INFO to:", directionsManager.getToMarkerNew().getTitle());
                isSearchToCustom = false;
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

        context = new GeoApiContext().setApiKey(getString(R.string.api_key_server));

        directionsManager = new DirectionsManager(context, mMap);

        initMarkerAddOptions();

        searchLayout = (LinearLayout) findViewById(R.id.search_layout);
        searchBtn = (ImageButton) findViewById(R.id.res_button);
        startNavButton = (FloatingActionButton ) findViewById(R.id.start_nav_button);
        searchBtn.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        searchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (from.getPrediction() == null || to.getPrediction() == null){
                    return;
                }

                startNavButton.setVisibility(View.GONE);

//                if (isSearchFromCurrentLocation()){
//                    Log.i("INFO:", "Search from current");
////                    directionsManager.setNewMarkerByCustomPrediction(false, tempMarker.getPosition(), (CustomAutoCompletePrediction)from.getPrediction());
////                    isSearchFromCustom = true;
//                }

                // hide keyboard on search
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // get from latlng and title
//                com.google.android.gms.maps.model.LatLng fromLatLng = null;
//                String fromTitle = null;
//                boolean useExistingFromMarker = false;
//                if ( isSearchFromCurrentLocation) {
//                    fromTitle = "Current Location";
//                    fromLatLng = new com.google.android.gms.maps.model.LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//                }
//                else if (fromSearchType == SearchType.MARKER){
//                    fromTitle = "Custom Origin";
//                    fromLatLng = directionsManager.getFromMarkerNew().getPosition();
//                    useExistingFromMarker = true;
//                }
//                else {
//                    try {
//                        PlaceDetails from_placeDetails = PlacesApi.placeDetails(context, from.getPrediction().getPlaceId()).await();
//                        fromLatLng = MapUtils.getGmsLatLngFromModel(from_placeDetails.geometry.location);
//                        fromTitle = from.getPrediction().getDescription();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                Log.i("INFO:", String.format("from lat %f lng %f", fromLatLng.latitude, fromLatLng.longitude));
//
//                // get To latlng and title
//                com.google.android.gms.maps.model.LatLng toLatLng = null;
//                String toTitle = null;
//                boolean useExistingToMarker = false;
//                if (toSearchType == SearchType.MARKER){
//                    toTitle = "Custom Origin";
//                    toLatLng = directionsManager.getFromMarkerNew().getPosition();
//                    useExistingToMarker = true;
//                }
//                else {
//                    try {
//                        PlaceDetails to_placeDetails = PlacesApi.placeDetails(context, to.getPrediction().getPlaceId()).await();
//                        toLatLng = MapUtils.getGmsLatLngFromModel(to_placeDetails.geometry.location);
//                        toTitle = to.getPrediction().getDescription();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }

//                Log.i("INFO:", String.format("from lat %f lng %f", toLatLng.latitude, toLatLng.longitude));

                directionsManager.getDirections(
                        directionsManager.getFromMarkerNew().getPosition(),
                        directionsManager.getToMarkerNew().getPosition(),
                        directionsManager.getFromMarkerNew().getTitle(),
                        directionsManager.getToMarkerNew().getTitle());

                Log.i("INFO:", String.format("Num calculated routes: %d", directionsManager.getCalculatedRoutes().length));


                allRoutes.updateBikeableRoutesAndMap(directionsManager.getCalculatedRoutes(), mMap);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(directionsManager.getDirectionBounds(), getResources()
                        .getInteger(R.integer.bound_padding)));
                allRoutes.findTelOFunMatchesToSourceAndDestination(mMap, directionsManager);

                graphDrawer = new PathElevationGraphDrawer(graph);
                for (BikeableRoute bikeableRoute : allRoutes.bikeableRoutes) {
                    ElevationResult[] results = bikeableRoute.elevationQuerier
                            .getElevationSamples(bikeableRoute.numOfElevationSamples);
                    graphDrawer.addSeries(results);
                }

                if ( isShowBikeRouteMatchesChecked ) {
                    showBikePathMatchesOnMap();
                }
                if (isShowCloseTelOFunStationsChecked){
                    allRoutes.showTelOFunDestinationMatchesOnMap();
                    allRoutes.showTelOFunSourceMatchesOnMap();
                }
                updateInfoTable();
                enableSlidingPanel();
                hideSearchView();

            }
        });

        startNavButton.setOnClickListener(new View.OnClickListener(){

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

    }

    private boolean isSearchFromCurrentLocation() {
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
    }

    private void enableSlidingPanel() {
        SlidingUpPanelLayout slidingUpLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingUpLayout.setPanelHeight(80);

    }

    private void updateInfoTable() {
        BikeableRoute currentRoute = allRoutes.getSelectedRoute();
        if ( currentRoute == null ) {
            clearInfoTable();
            return;
        }

        clearInfoTable();

        pathDurationTextView.setText(String.format("%s", currentRoute.getDurationString()));
        pathPercTextView.setText(String.format("%.1f", currentRoute.getBikePathPercentage()*100));
        pathDistanceTextView.setText(String.format("%s", currentRoute.getDistanceString()));
        pathUphillAverageTextView.setText(String.format("%.2f", currentRoute.getAverageUphillDegree()));

    }

    private void clearInfoTable() {
        pathDurationTextView.setText("");
        pathPercTextView.setText("");
        pathDistanceTextView.setText("");
        pathUphillAverageTextView.setText("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_central, menu);

        menuSearch = menu.findItem(R.id.action_search);
        menuSearch.setVisible(false);

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
            case R.id.action_settings:
                return true;
            case R.id.iria_bike_path_cb:
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
                }
                else{
                    item.setChecked(false);
                    if (!IriaData.isDataReceived){
                        return true;
                    }
                    IriaData.removeBikePathFromMap();
                }
                return true;

            case R.id.iria_bike_path_mathches:
                if (!item.isChecked()){
                    if (!IriaData.isDataReceived){
                        Toast.makeText(
                                CentralActivity.this,
                                "Failed to get Tel-Aviv Municipality Data",
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    item.setChecked(true);
                    isShowBikeRouteMatchesChecked = true;
                    showBikePathMatchesOnMap();
                }

                else{
                    item.setChecked(false);
                    isShowBikeRouteMatchesChecked = false;
                    if (!IriaData.isDataReceived){
                        return true;
                    }
                    removeBikePathMatchesFromMap();
                }
                return true;

            case R.id.iria_telOFun_cb:
                if (!item.isChecked()){
                    if (!IriaData.isDataReceived){
                        Toast.makeText(
                                CentralActivity.this,
                                "Failed to get Tel-Aviv Municipality Data",
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    item.setChecked(true);
                    IriaData.addTelOFunToMap(mMap);
                    IriaData.showTelOFunOnMap();
                }
                else{
                    item.setChecked(false);
                    if (!IriaData.isDataReceived){
                        return true;
                    }

                    IriaData.removeTelOFunFromMap();
                }
                return true;
            case R.id.iria_telOFun_matches:
                if (!item.isChecked()){
                    if (!IriaData.isDataReceived){
                        Toast.makeText(
                                CentralActivity.this,
                                "Failed to get Tel-Aviv Municipality Data",
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    item.setChecked(true);
                    isShowCloseTelOFunStationsChecked = true;
                    allRoutes.showTelOFunSourceMatchesOnMap();
                    allRoutes.showTelOFunDestinationMatchesOnMap();
                }

                else{
                    item.setChecked(false);
                    isShowCloseTelOFunStationsChecked = false;
                    if (!IriaData.isDataReceived){
                        return true;
                    }
                    allRoutes.hideTelOFunSourceMatchesOnMap();
                    allRoutes.hideTelOFunDestinationMatchesOnMap();
                    isShowCloseTelOFunStationsChecked = false;
                }
                return true;
            case R.id.action_search:
                showSearchView();
                return true;
            case R.id.action_feedback:
                Intent Email = new Intent(Intent.ACTION_SEND);
                Email.setType("text/email");
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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(placeToFocusOn));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15f));

        mMap.setOnMapClickListener((new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng clickLatLng) {
                Log.i("inside listener begin", "inside listener begin2");
                markerOptsLayout.setVisibility(View.GONE);
                if (tempMarker != null)
                    tempMarker.remove();

                if (!allRoutes.bikeableRoutes.isEmpty()) {
                    MapUtils.selectClickedRoute(allRoutes, clickLatLng);

                    if (allRoutes.getSelectedRouteIndex() >= 0) {
                        graphDrawer.colorSeriesByIndex(allRoutes.getSelectedRouteIndex());
                        updateInfoTable();
                        if (isSearchFromCurrentLocation) {
                            startNavButton.setVisibility(View.VISIBLE);
                        }

                    }
                }
            }
        }
        ));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                           @Override
                                           public void onMapLongClick(LatLng latLng) {
                                               if (tempMarker != null) {
                                                   tempMarker.remove();
                                               }
                                               tempMarker = mMap.addMarker(new MarkerOptions()
                                                       .position(latLng));
                                               Log.i("INFO:", String.format("Added marker %f", tempMarker.getPosition().longitude));
                                               markerOptsLayout.setVisibility(View.VISIBLE);
                                           }
                                       }
        );

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
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
        boolean mRequestingLocationUpdates = true;
        if (mRequestingLocationUpdates) {
            createLocationRequest();
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.i("INFO", String.format("Current loction lat: %f", mCurrentLocation.getLatitude()));
        Log.i("INFO", String.format("Current location lang %f", mCurrentLocation.getLongitude()));
        updateUI();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        /* ask to turn on location //TODO: make it appear when needed
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
    private void initMarkerAddOptions() {

        markerOptsLayout =  (LinearLayout) findViewById(R.id.marker_opts);
        Button markerOriginBtn = (Button) findViewById(R.id.marker_origin);
        Button markerDestBtn = (Button) findViewById(R.id.marker_dest);
        Button markerCancelBtn = (Button) findViewById(R.id.marker_cancel);

        markerOriginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayout.setVisibility(View.VISIBLE);
                markerOptsLayout.setVisibility(View.GONE);
                CustomAutoCompletePrediction newPrediction = new CustomAutoCompletePrediction("Custom Origin", tempMarker.getPosition().toString());
                from.setPrediction(newPrediction, true);
                directionsManager.setNewMarkerByCustomPrediction(true, tempMarker.getPosition(), newPrediction);
                isSearchFromCustom = true;
                tempMarker.remove();
                tempMarker = null;
            }
        });

        markerDestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayout.setVisibility(View.VISIBLE);
                markerOptsLayout.setVisibility(View.GONE);
                CustomAutoCompletePrediction newPrediction = new CustomAutoCompletePrediction("Custom Destination", tempMarker.getPosition().toString());
                to.setPrediction(newPrediction, true);
                directionsManager.setNewMarkerByCustomPrediction(false, tempMarker.getPosition(), newPrediction);
                isSearchToCustom = true;
                tempMarker.remove();
                tempMarker = null;
            }
        });

        markerCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerOptsLayout.setVisibility(View.GONE);
                tempMarker.remove();
            }
        });
    }
}
