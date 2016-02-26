package com.nnys.bikeable;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;


public class MarkersInfoWindowAdapter implements InfoWindowAdapter {

    Context context;
    private View popup = null;
    private MapWrapperLayout mapWrapperLayout = null;
    TextView tvTitle;
    TextView tvBike;
    TextView tvStands;
    Button infoButton1;

    MarkersInfoWindowAdapter(LayoutInflater inflater, MapWrapperLayout mapWrapperLayout, final Context context) {

        this.mapWrapperLayout = mapWrapperLayout;
        this.context = context;

        popup = inflater.inflate(R.layout.info_window_layout, null);

        infoButton1 = (Button)popup.findViewById(R.id.info_button1);;
        tvTitle= (TextView) popup.findViewById(R.id.title);;
        tvBike =  (TextView) popup.findViewById(R.id.bike_data);
        tvStands =  (TextView) popup.findViewById(R.id.stands_data);

        infoButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Toast.makeText(context, "Button 1 clicked", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return (null);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {
        //if (popup == null) {
            //popup = inflater.inflate(R.layout.info_window_layout, null);
        //}
        mapWrapperLayout.setMarkerWithInfoWindow(marker, popup);


        if (marker.getTitle().equals("TelOFun")) {

            int stationId = Integer.parseInt(marker.getSnippet());
            TelOFunStation station = IriaData.getTelOfanStationsDict().get(stationId);
            int bikesAvailable = station.getNumOfBikesAvailable();
            int standsAvailable = station.getNumOfStandsAvailable();
            String stationName = station.getStationName();

            tvTitle.setText(stationName);
            tvBike.setVisibility(View.VISIBLE);
            tvBike.setText("bikes available: " + bikesAvailable);
            tvStands.setVisibility(View.VISIBLE);
            tvStands.setText("stands available: " + standsAvailable);
        }
        else {
            tvTitle.setText(marker.getTitle());
            tvBike.setVisibility(View.GONE);
            tvStands.setVisibility(View.GONE);
        }

        return (popup);
    }
}
