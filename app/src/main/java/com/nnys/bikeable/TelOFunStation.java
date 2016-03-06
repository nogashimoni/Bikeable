package com.nnys.bikeable;

import com.google.android.gms.maps.model.LatLng;

/**
 * This class represents a telOfun station, with all the information needed.
 */
public class TelOFunStation {
    private LatLng coordinates;
    private int id;
    private int orderId;
    private int numOfStands;
    private int numOfBikesAvailable;
    private int numOfStandsAvailable;
    private String stationName;

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

    public String getStationName () {
        return stationName;
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

    public void setStationName (String name){
        stationName = name;
    }
}
