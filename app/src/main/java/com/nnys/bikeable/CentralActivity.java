package com.nnys.bikeable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.GeoApiContext;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.maps.model.ElevationResult;
import com.jjoe64.graphview.GraphView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;


public class CentralActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    protected GoogleApiClient mGoogleApiClient;
    protected GeoApiContext context;
    protected DirectionsManager directionsManager = null;

    private AllRoutes allRoutes;

    private Button searchBtn, clearBtn, showGraphBtn, bikePathButton, singleBikePathButton,
            startNavButton;

    private ArrayList<com.google.maps.model.LatLng> points = new ArrayList<>();
    private GoogleMap mMap;
    private ClearableAutoCompleteTextView to, from;

    private PopupWindow graphPopupWindow;
    private LayoutInflater layoutInflater;

    private IriaBikePath iriaBikePath = null;
    private PathElevationGraphDrawer graphDrawer;
    private GraphView graph;
    private IriaBikePath iriaBikeSinglePath = null;
    private BikePathCalculator pathCalculator = null;
    private int pathNumber;
    private boolean isPathCalculatorInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // to enable getting data from Tel Aviv muni website
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        setContentView(R.layout.central_activity_layout);

        //disableSlidingPanel();

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
                if (from.getPrediction() == null || to.getPrediction() == null)
                    return;
                if (directionsManager != null)
                    directionsManager.clearMarkersFromMap();

                // hide keyboard on search
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(v.getWindowToken(), 0);
                Log.i("INFO: ", from.getPrediction().getDescription());
                Log.i("INFO: ", to.getPrediction().getDescription());
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
                enableSlidingPanel(); //TODO doesn't work

            }
        });


        bikePathButton = (Button) findViewById(R.id.bike_button);
        bikePathButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (iriaBikePath == null){
                    try {
                        iriaBikePath = new IriaBikePath(mMap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }
                if (iriaBikePath.isBikePathShown) {
                    iriaBikePath.removeBikePathFromMap();
                }
                else {
                    iriaBikePath.showBikePathOnMap();
                }

            }
        });


        singleBikePathButton = (Button) findViewById(R.id.single_bike_button);
        singleBikePathButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (directionsManager == null){
                    return;
                }
                if (iriaBikePath == null){
                    try {
                        iriaBikePath = new IriaBikePath(mMap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }

                ArrayList <Polyline> iriaPaths = iriaBikePath.getPaths();


                int numOfroutes = allRoutes.getNumRoutes();
                for (int i = 0; i < numOfroutes; i++) {
                    BikePathCalculator pathCalculator = new BikePathCalculator(allRoutes.getAllRoutes().get(i).routePolylineOptions,
                            directionsManager.getDirectionBounds(), iriaPaths, mMap, allRoutes.getAllRoutes().get(i).directionsRoute);
                    float bikePathDistance = pathCalculator.getTotalBikePathDitance();
                    System.out.println("distanceeeeeeeeeeeeeeeeeeee: " + bikePathDistance);
                    long routeDistance = pathCalculator.getCurrRouteDistance();

                    System.out.println("distance: " + routeDistance);

                    float bikePathPersentage = pathCalculator.getBikePathPercentage(routeDistance, bikePathDistance);
                    System.out.println("percentage: " + bikePathPersentage);
                }

            }
        });

        startNavButton = (Button) findViewById(R.id.start_nav_button);
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

    private void disableSlidingPanel() {
        SlidingUpPanelLayout slidingUpLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingUpLayout.setEnabled(false);
        LinearLayout srolling_part = (LinearLayout)findViewById(R.id.scrolling_part);
        srolling_part.setVisibility(View.INVISIBLE);
    }

    private void enableSlidingPanel() {
        SlidingUpPanelLayout slidingUpLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingUpLayout.setEnabled(true);
        LinearLayout scrolling_part = (LinearLayout)findViewById(R.id.scrolling_part);
        scrolling_part.setVisibility(View.VISIBLE);
        scrolling_part.requestLayout();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

                    if (allRoutes.getSelectedRouteIndex() >= 0)
                        graphDrawer.colorSeriosByIndex(allRoutes.getSelectedRouteIndex());
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
    }
}
