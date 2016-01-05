package com.nnys.bikeable;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
/**
 * Created by Yishay on 1/4/2016.
 */
public class MarkersInfoWindowAdapter implements InfoWindowAdapter {

    private View popup = null;
    private LayoutInflater inflater = null;

    MarkersInfoWindowAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
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

        if (marker.getTitle().equals("TelOFun")) {
            if (popup == null) {
                popup = inflater.inflate(R.layout.info_window_layout, null);
            }
            TextView tvBike;
            int stationId = Integer.parseInt(marker.getSnippet());
            TelOFunStation station = IriaData.getTelOfanStationsDict().get(stationId);
            int bikesAvailable = station.getNumOfBikesAvailable();
            int standsAvailable = station.getNumOfStandsAvailable();
            String stationName = station.getStationName();

            tvBike = (TextView) popup.findViewById(R.id.title);
            tvBike.setText(stationName);
            tvBike = (TextView) popup.findViewById(R.id.bike_data);
            tvBike.setText("bikes available: " + bikesAvailable);
            tvBike = (TextView) popup.findViewById(R.id.stands_data);
            tvBike.setText("stands available: " + standsAvailable);
        }
        else {
            if (popup == null) {
                popup = inflater.inflate(R.layout.info_window_place, null);
            }
            TextView tvPlace;
            tvPlace = (TextView) popup.findViewById(R.id.title);
            tvPlace.setText(marker.getTitle());
        }

        return (popup);
    }
}
