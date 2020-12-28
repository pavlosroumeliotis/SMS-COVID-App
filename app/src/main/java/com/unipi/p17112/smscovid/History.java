package com.unipi.p17112.smscovid;

public class History {
    String longitude, latitude, timestamp;
    int codeID;

    public History(String longitude, String latitude, String timestamp, int codeID){
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        this.codeID = codeID;
    }

    public History(){

    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getCodeID() {
        return codeID;
    }
}
