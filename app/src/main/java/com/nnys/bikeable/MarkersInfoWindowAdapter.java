package com.nnys.bikeable;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;


public class MarkersInfoWindowAdapter implements InfoWindowAdapter {

    private View popup = null;
    private LayoutInflater inflater = null;
    TextView tvTitle;
    TextView tvBike;
    TextView tvStands;

    MarkersInfoWindowAdapter(LayoutInflater inflater) {

        this.inflater = inflater;

        popup = inflater.inflate(R.layout.info_window_layout, null);

        tvTitle= (TextView) popup.findViewById(R.id.title);;
        tvBike =  (TextView) popup.findViewById(R.id.bike_data);
        tvStands =  (TextView) popup.findViewById(R.id.stands_data);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return (null);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {

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
