package com.example.mymap.api;

public class Destination {
    private double latitude ,longitude ,heading ;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    @Override
    public String toString() {
        return "Destination{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", heading=" + heading +
                '}';
    }
}
