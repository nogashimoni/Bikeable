package com.nnys.bikeable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.maps.model.DirectionsRoute;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.EncodedPolyline;
import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;


public class CentralActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    protected GoogleApiClient mGoogleApiClient;
    protected GeoApiContext context;
    protected DirectionsManager directionsManager =  null;

    private PlaceAutocompleteAdapter from_adapter;
    private PlaceAutocompleteAdapter to_adapter;
    private AutocompletePrediction to_prediction = null, from_prediction= null;
    private Button searchBtn, clearBtn, showGraphBtn;
    private DirectionsRoute[] routes;
    private ArrayList<com.google.maps.model.LatLng> points = new ArrayList<>();
    private GoogleMap mMap;
    private AutoCompleteTextView to, from;

    private PopupWindow graphPopupWindow;
    private LayoutInflater layoutInflater;


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

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);
        from.setText("");
        to.setText("");
        final TextView resultText = (TextView) findViewById(R.id.to_result);

        from_adapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                null);
        from.setAdapter(from_adapter);
        to_adapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                null);
        to.setAdapter(to_adapter);

        final MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        from.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                from_prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
            }
        });

        to.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                to_prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
            }
        });

        context = new GeoApiContext().setApiKey(getString(R.string.api_key_server));

        searchBtn = (Button) findViewById(R.id.res_button);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                if (from_prediction == null || to_prediction == null)
                    return;
                mMap.clear();
                directionsManager = new DirectionsManager(context, from_prediction, to_prediction);
//                directionsManager.drawRoute(0, mMap);
                directionsManager.drawAllRoutes(mMap);
                directionsManager.drawRouteMarkers(mMap);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(directionsManager.getDirectionBounds(), getResources()
                        .getInteger(R.integer.bound_padding)));
            }
        });

        clearBtn = (Button) findViewById(R.id.clear_button);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                to_prediction = null;
                from_prediction= null;
                directionsManager = null;
                from.setText("");
                to.setText("");
                mMap.clear();
            }

        });

        showGraphBtn = (Button) findViewById(R.id.show_graph_button);
        showGraphBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (directionsManager == null || directionsManager.getSelectedRouteIndex() == -1) {
                    return;
                }
                EncodedPolyline currPathEncPoly = directionsManager.getCurrRouteEncPolyline();
                PathElevationQuerier querier = new PathElevationQuerier(currPathEncPoly);
                long distance = directionsManager.getCurrRouteDistance();
                int numOfSamples = querier.calcNumOfSamplesForXmetersIntervals(distance,20);
                ElevationResult[] results = querier.getElevationSamples(numOfSamples);

                layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.graph_popup,null);

                GraphView graph = (GraphView) container.findViewById(R.id.altitude_graph);
                PathElevationGraphDrawer graphDrawer = new PathElevationGraphDrawer(graph, results);
                graphDrawer.drawGraph();

                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                int width = dm.widthPixels;
                int height = dm.heightPixels;

                graphPopupWindow = new PopupWindow(container, width,300, true);
                graphPopupWindow.showAtLocation(findViewById(R.id.centralLayout), Gravity.BOTTOM, 0, 0);

                container.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        graphPopupWindow.dismiss();
                        return false;
                    }
                });
            }
        });
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
        LatLng sharonHome = new LatLng(32.110277, 34.794866);
        LatLng nearsharonHome = new LatLng(32.110277, 34.994866);
        mMap.addMarker(new MarkerOptions()
                        .title("Sharon's Home")
                        .snippet("The coolest place in Tel Aviv!")
                        .position(sharonHome)
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sharonHome));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15f));

        mMap.setOnMapClickListener((new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng clickLatLng) {
                Log.i("inside listener begin", "inside listener begin2");
                if (directionsManager != null) {
                    MapUtils.selectClickedRoute(directionsManager, clickLatLng);
                }
            }
        }
        ));

        mMap.setOnMarkerClickListener( new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });



    }
}
