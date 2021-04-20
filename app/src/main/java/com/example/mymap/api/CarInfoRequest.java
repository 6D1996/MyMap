package com.example.mymap.api;

public class CarInfoRequest {

    private String userId="usertest001";
    private String vin="001";
    private String dataFields=
            "timeStamp,brake,status,parkingTime,position3d,speed3d,accelerate3d,gears,parkingType";

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getDataFields() {
        return dataFields;
    }

    public void setDataFields(String dataFields) {
        this.dataFields = dataFields;
    }
}
