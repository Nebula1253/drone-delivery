package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.List;

@JsonIgnoreProperties("name")
public record LngLat(
        @JsonProperty("longitude")
        double lng,
        @JsonProperty("latitude")
        double lat) {
    private static final double DIST_TOLERANCE = 0.00015;

    public boolean inCentralArea() throws IOException {
        List<LngLat> areaPoints = CentralArea.getInstance().getCentralArea();

        // maybe you'll have to modify this to account for an n-gon?
        LngLat bottomLeft = areaPoints.get(1);
        LngLat topRight = areaPoints.get(areaPoints.size() - 1);

        return (this.lat >= bottomLeft.lat && this.lat<= topRight.lat && this.lng >= bottomLeft.lng && this.lng <= topRight.lng);
    }
    public double distanceTo(LngLat other) {
        return Math.sqrt(((lng - other.lng) * (lng - other.lng)) +
                ((lat - other.lat) * (lat - other.lat)));
    }

    public boolean closeTo(LngLat other) {
        return (this.distanceTo(other) < DIST_TOLERANCE);
    }

    public LngLat nextPosition(CompassDirection dir) {
        if (dir == null) { return this; }

        double angle = dir.ordinal() * (360f / CompassDirection.values().length) * (Math.PI/180);
        double yChange = Math.sin(angle) * DIST_TOLERANCE;
        double xChange = Math.cos(angle) * DIST_TOLERANCE;
        return new LngLat(this.lng + xChange, this.lat + yChange);
    }
}
