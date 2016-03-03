package com.nnys.bikeable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

/**
 * Created by Yishay on 3/3/2016.
 */
@DynamoDBTable(tableName = "Constants")
public class ConstantsFromTable {
    private String stringKey;
    private String stringValue;

    @DynamoDBHashKey(attributeName = "StringKey")
    public String getStringKey() {
        return stringKey;
    }

    public void setStringKey(String stringKey) {
        this.stringKey = stringKey;
    }

    @DynamoDBAttribute(attributeName = "StringValue")
    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
}
