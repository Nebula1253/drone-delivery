package uk.ac.ed.inf;

public record LngLat(double lng, double lat) {
    public boolean inCentralArea() {
        String baseURL = "https://ilp-rest.azurewebsites.net/centralArea";

        // somehow get an array of strings in json format, convert to array of lnglats and check whether falls within boundaries
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
