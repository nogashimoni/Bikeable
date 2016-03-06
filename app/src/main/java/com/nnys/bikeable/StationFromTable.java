package com.nnys.bikeable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

/**
 * This class enables to get the TelOFun table data from dynamoDB
 */
@DynamoDBTable(tableName = "TelOFun")
public class StationFromTable {
    private Integer stationID;
    private String name;
    private Integer bikesAvailable;
    private Integer standsAvailable;
    private String timeStamp;

    @DynamoDBHashKey(attributeName = "StationID")
    public Integer getStationID() {
        return stationID;
    }

    public void setStationID(Integer stationID) {
        this.stationID = stationID;
    }

    @DynamoDBAttribute(attributeName = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "BikesAvailable")
    public Integer getBikesAvailable() {
        return bikesAvailable;
    }

    public void setBikesAvailable(Integer bikesAvailable) {
        this.bikesAvailable = bikesAvailable;
    }

    @DynamoDBAttribute(attributeName = "StandsAvailable")
    public Integer getStandsAvailable() {
        return standsAvailable;
    }

    public void setStandsAvailable(Integer standsAvailable) {
        this.standsAvailable = standsAvailable;
    }

    @DynamoDBAttribute(attributeName = "TimeStamp")
    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}