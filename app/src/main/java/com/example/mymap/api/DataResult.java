package com.example.mymap.api;

public class DataResult {

    private String timeStamp="Initial";
    private String brake="Initial";
    private String speed3d="Initial";
    private String parkingType="Initial";
    private String parkingTime="Initial";
    private String position3d="44,125,0";
    private String gears="Initial";
    private String status="Initial";
    private String accelerate3d="Initial";

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getBrake() {
        return brake;
    }

    public void setBrake(String brake) {
        this.brake = brake;
    }

    public String getSpeed3d() {
        return speed3d;
    }

    public void setSpeed3d(String speed3d) {
        this.speed3d = speed3d;
    }

    public String getParkingType() {
        return parkingType;
    }

    public void setParkingType(String parkingType) {
        this.parkingType = parkingType;
    }

    public String getParkingTime() {
        return parkingTime;
    }

    public void setParkingTime(String parkingTime) {
        this.parkingTime = parkingTime;
    }

    public String getPosition3d() {
        return position3d;
    }

    public void setPosition3d(String position3d) {
        this.position3d = position3d;
    }

    public String getGears() {
        return gears;
    }

    public void setGears(String gears) {
        this.gears = gears;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccelerate3d() {
        return accelerate3d;
    }

    public void setAccelerate3d(String accelerate3d) {
        this.accelerate3d = accelerate3d;
    }

    @Override
    public String toString() {
        return "DataResult{" +
                "timeStamp='" + timeStamp + '\'' +
                ", brake='" + brake + '\'' +
                ", speed3d='" + speed3d + '\'' +
                ", parkingType='" + parkingType + '\'' +
                ", parkingTime='" + parkingTime + '\'' +
                ", position3d='" + position3d + '\'' +
                ", gears='" + gears + '\'' +
                ", status='" + status + '\'' +
                ", accelerate3d='" + accelerate3d + '\'' +
                '}';
    }
}
