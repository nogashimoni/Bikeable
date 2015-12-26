package com.nnys.bikeable;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.GeoApiContext;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.maps.model.ElevationResult;
import com.jjoe64.graphview.GraphView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;


public class CentralActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    protected GoogleApiClient mGoogleApiClient;
    protected GeoApiContext context;
    protected DirectionsManager directionsManager = null;

    private AllRoutes allRoutes;

    private Button searchBtn, singleBikePathButton;

    private ArrayList<com.google.maps.model.LatLng> points = new ArrayList<>();
    private GoogleMap mMap;
    private ClearableAutoCompleteTextView to, from;


    private PathElevationGraphDrawer graphDrawer;
    private GraphView graph;

    private TextView pathDurationTextView;
    private TextView pathPercTextView;
    private TextView pathDistanceTextView;
    private TextView pathUphillAverageTextView;

    private boolean isShowBikeRouteMatchesChecked = false;


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
        from.setAdapter(new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                null));
        to.setAdapter(new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                null));

        allRoutes = new AllRoutes();
        graph = (GraphView)findViewById(R.id.altitude_graph);

        final MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = new GeoApiContext().setApiKey(getString(R.string.api_key_server));

        searchBtn = (Button) findViewById(R.id.res_button);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                removeBikePathMatchesFromMap(); // todo check if it's ok even if they're not colored

                if (from.getPrediction() == null || to.getPrediction() == null)
                    return;
                if (directionsManager != null)
                    directionsManager.clearMarkersFromMap();

                // hide keyboard on search
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(v.getWindowToken(), 0);

                directionsManager = new DirectionsManager(context, from.getPrediction(), to.getPrediction());
                allRoutes.updateBikeableRoutesAndMap(directionsManager.getCalculatedRoutes(), mMap);
                directionsManager.drawRouteMarkers(mMap);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(directionsManager.getDirectionBounds(), getResources()
                        .getInteger(R.integer.bound_padding)));

                graphDrawer = new PathElevationGraphDrawer(graph);
                for (BikeableRoute bikeableRoute: allRoutes.bikeableRoutes) {
                    ElevationResult[] results = bikeableRoute.elevationQuerier
                            .getElevationSamples(bikeableRoute.numOfElevationSamples);
                    graphDrawer.addSeries(results);
                }

                if ( isShowBikeRouteMatchesChecked ) {
                    showBikePathMatchesOnMap();
                }
                updateInfoTable();
                enableSlidingPanel(); //TODO doesn't work

            }
        });

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

        pathDurationTextView.setText(String.format("%d", currentRoute.getDuration()));
        pathPercTextView.setText(String.format("%f", currentRoute.getBikePathPercentage()));
        pathDistanceTextView.setText(String.format("%d", currentRoute.getDistance()));
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void removeBikePathMatchesFromMap() {
        if (allRoutes.bikeableRoutes.isEmpty()) { // no routes on map
            return;
        }
        else {
            for (BikeableRoute route : allRoutes.bikeableRoutes) {
                route.removeBikePathFromMap();;
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
        LatLng tau = new LatLng(32.113523, 34.804399);
//        mMap.addMarker(new MarkerOptions()
//                        .title("Tel-Aviv University")
//                        .position(tau)
//        );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(tau));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15f));

        mMap.setOnMapClickListener((new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng clickLatLng) {
                Log.i("inside listener begin", "inside listener begin2");
                if (!allRoutes.bikeableRoutes.isEmpty()) {
                    MapUtils.selectClickedRoute(allRoutes, clickLatLng);

                    if (allRoutes.getSelectedRouteIndex() >= 0) {
                        graphDrawer.colorSeriosByIndex(allRoutes.getSelectedRouteIndex());
                        updateInfoTable();
                    }
                }
            }
        }
        ));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });


        /*double[][] arr2 =  {{32.141237, 34.800872}, {32.141489, 34.800135}, {32.141641, 34.799725}, {32.141962, 34.798795},
                {32.142071, 34.798485}, {32.142149, 34.798263}, {32.142359, 34.797588}, {32.142451, 34.797285}};
        ArrayList<com.google.android.gms.maps.model.LatLng> points = new ArrayList<>();
        for (int i = 0; i < 8; i++){
            points.add(new LatLng(arr2[i][0], arr2[i][1]));
        }
        PolylineOptions line = new PolylineOptions();
        com.google.android.gms.maps.model.LatLng currPoint;
        for (com.google.android.gms.maps.model.LatLng point : points) {
            currPoint = new com.google.android.gms.maps.model.LatLng(point.latitude, point.longitude);
            line.add(currPoint);
        }
        mMap.addPolyline(line);*/



    }
}
