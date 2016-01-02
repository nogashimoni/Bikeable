package com.nnys.bikeable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Yishay on 1/2/2016.
 */
public class TelOFunStation {
    private LatLng coordinates;
    private int id;
    private int orderId;
    private int numOfStands;
    private int numOfBikesAvailable;
    private int numOfStandsAvailable;

    public TelOFunStation(LatLng coordinatesOfStation, int stationId, int stationOrderId){
        coordinates = coordinatesOfStation;
        id = stationId;
        orderId = stationOrderId;
    }

    public LatLng getCoordinates (){
        return coordinates;
    }

    public int getId (){
        return id;
    }

    public int getOrderId (){
        return orderId;
    }

    public int getNumOfStands (){
        return numOfStands;
    }

    public int getNumOfStandsAvailable (){
        return numOfStandsAvailable;
    }

    public int getNumOfBikesAvailable (){
        return numOfBikesAvailable;
    }

    public void setNumOfStands (int stands){
        numOfStands = stands;
    }

    public void setNumOfStandsAvailable (int standsAvailable){
        numOfStandsAvailable = standsAvailable;
    }

    public void setNumOfBikesAvailable (int bikesAvailable){
        numOfBikesAvailable = bikesAvailable;
    }
}
