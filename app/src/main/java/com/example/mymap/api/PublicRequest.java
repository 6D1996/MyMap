package com.example.mymap.api;

public class PublicRequest {
    public String hostURL="http://vehicleroadcloud.faw.cn:60443/backend/appBackend/";
    private String requestId;
    private String userId="6DAndroid";
    private String vin="test_car";

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

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
}
