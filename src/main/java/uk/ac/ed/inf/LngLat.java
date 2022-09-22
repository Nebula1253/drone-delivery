package uk.ac.ed.inf;

public record LngLat(double lng, double lat) {
    private static final double DIST_TOLERANCE = 0.00015;

    public boolean inCentralArea() {
        String baseURL = "https://ilp-rest.azurewebsites.net/centralArea";

        // @TODO: somehow get an array of strings in json format, convert to array of lnglats and check whether falls within boundaries
        return false;
    }
    public double distanceTo(LngLat other) {
        return Math.sqrt(((lng - other.lng) * (lng - other.lng)) +
                ((lat - other.lat) * (lat - other.lat)));
    }

    public boolean closeTo(LngLat other) {
        return (this.distanceTo(other) < DIST_TOLERANCE);
    }

    public LngLat nextPosition(CompassDirection dir) {
        double angle = dir.ordinal() * 22.5 * (Math.PI/180);
        double yChange = Math.sin(angle) * DIST_TOLERANCE;
        double xChange = Math.cos(angle) * DIST_TOLERANCE;
        return new LngLat(this.lng + xChange, this.lat + yChange);
    }
}
