package com.nnys.bikeable;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

/**
 * This class extends markers' default infoWindow, to enable showing custom layout
 * in the infoWindow
 */
public class MarkersInfoWindowAdapter implements InfoWindowAdapter {

    private View popup = null;
    private MapWrapperLayout mapWrapperLayout = null;

    TextView tvTitle;
    TextView tvBike;
    TextView tvStands;
    TextView infoOrigBtn;
    TextView infoDstBtn;

    MarkersInfoWindowAdapter(LayoutInflater inflater, MapWrapperLayout mapWrapperLayout) {

        this.mapWrapperLayout = mapWrapperLayout;

        popup = inflater.inflate(R.layout.info_window_layout, null);

        infoOrigBtn = (TextView)popup.findViewById(R.id.info_orig_btn);
        infoDstBtn = (TextView)popup.findViewById(R.id.info_dest_btn);
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

        mapWrapperLayout.setMarkerWithInfoWindow(marker, popup);

        if (marker.getTitle().equals("TelOFun")) {

            int stationId = Integer.parseInt(marker.getSnippet());
            TelOFunStation station = IriaData.getTelOfanStationsDict().get(stationId);
            int bikesAvailable = station.getNumOfBikesAvailable();
            int standsAvailable = station.getNumOfStandsAvailable();
            String stationName = station.getStationName();


            tvTitle.setText(String.format("%s (%s)", stationName, marker.getSnippet()));
            tvTitle.setVisibility(View.VISIBLE);
            tvBike.setText("Bikes available: " + bikesAvailable);
            tvBike.setVisibility(View.VISIBLE);
            tvStands.setText("Stands available: " + standsAvailable);
            tvStands.setVisibility(View.VISIBLE);
        }
        else {
            if (marker.getTitle().equals("Temp Marker")){
                tvTitle.setVisibility(View.GONE);
            }
            else {
                tvTitle.setText(marker.getTitle());
                tvTitle.setVisibility(View.VISIBLE);
            }
            tvBike.setVisibility(View.GONE);
            tvStands.setVisibility(View.GONE);
        }

        return (popup);
    }

    public TextView getInfoOrigBtn() {  return infoOrigBtn;  }

    public TextView getInfoDstBtn() {  return infoDstBtn;  }
}
