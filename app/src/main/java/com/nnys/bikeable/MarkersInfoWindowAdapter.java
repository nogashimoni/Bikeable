package com.nnys.bikeable;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;


public class MarkersInfoWindowAdapter implements InfoWindowAdapter {

    private View popup = null;
    private LayoutInflater inflater = null;
    TextView tvTitle;
    TextView tvBike;
    TextView tvStands;
    LinearLayout markerOptsInfoWindow;
    LinearLayout markerAnchor;

    MarkersInfoWindowAdapter(LayoutInflater inflater) {

        this.inflater = inflater;

        popup = inflater.inflate(R.layout.info_window_layout, null);

        tvTitle= (TextView) popup.findViewById(R.id.title);;
        tvBike =  (TextView) popup.findViewById(R.id.bike_data);
        tvStands =  (TextView) popup.findViewById(R.id.stands_data);
        markerOptsInfoWindow =  (LinearLayout) popup.findViewById(R.id.marker_info_window);
        markerAnchor = (LinearLayout) popup.findViewById(R.id.marker_anchor);
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

            int stationId = Integer.parseInt(marker.getSnippet());
            TelOFunStation station = IriaData.getTelOfanStationsDict().get(stationId);
            int bikesAvailable = station.getNumOfBikesAvailable();
            int standsAvailable = station.getNumOfStandsAvailable();
            String stationName = station.getStationName();

            markerOptsInfoWindow.setVisibility(View.GONE);
            tvTitle.setText(stationName);
            tvBike.setVisibility(View.VISIBLE);
            tvBike.setText("bikes available: " + bikesAvailable);
            tvStands.setVisibility(View.VISIBLE);
            tvStands.setText("stands available: " + standsAvailable);
        }
        else if (marker.getTitle().equals("Temp Marker")) {
            tvBike.setVisibility(View.GONE);
            tvStands.setVisibility(View.GONE);
            tvTitle.setText(marker.getTitle());
            markerOptsInfoWindow.setVisibility(View.VISIBLE);
        }
        else {
            markerOptsInfoWindow.setVisibility(View.GONE);
            tvTitle.setText(marker.getTitle());
            tvBike.setVisibility(View.GONE);
            tvStands.setVisibility(View.GONE);
        }

        return (popup);
    }

    public LinearLayout getMarkerAnchor() {
        markerAnchor = (LinearLayout) popup.findViewById(R.id.marker_anchor);
        return markerAnchor;
    }
}
