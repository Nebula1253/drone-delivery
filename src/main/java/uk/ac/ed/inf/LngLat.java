package uk.ac.ed.inf;

import java.net.URL;

public class LngLat {
    public double lng, lat;

    public LngLat(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public boolean inCentralArea() {
        String baseURL = "https://ilp-rest.azurewebsites.net/";

        return false;
    }

    public double distanceTo(LngLat other) {
        return Math.sqrt(((lng - other.lng) * (lng - other.lng)) +
                         ((lat - other.lat) * (lat - other.lat)));
    }

    public boolean closeTo(LngLat other) {
        return (this.distanceTo(other) < 0.0015);
    }

    public LngLat nextPosition() {
        return null;
    }
}
