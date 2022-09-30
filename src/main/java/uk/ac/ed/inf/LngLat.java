package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents a location in the world, with longitude and latitude coordinates
 * @param lng The longitude, in degrees of the location
 * @param lat The latitude, in degrees, of the location
 */
@JsonIgnoreProperties("name")
public record LngLat(
        @JsonProperty("longitude")
        double lng,
        @JsonProperty("latitude")
        double lat) {
    private static final double DIST_TOLERANCE = 0.00015;

    /**
     * Checks whether this location is within the central area of the University campus, based on coordinates retrieved
     * from a REST server
     * @return A boolean value showing whether the point is within the central area
     * @throws IOException if the data retrieval fails
     */
    public boolean inCentralArea() throws IOException {
        ArrayList<LngLat> areaPoints = CentralArea.getInstance().getCentralArea();

        System.out.println(areaPoints.get(1));

        // TODO: modify to account for n-sided polygons
        LngLat bottomLeft = areaPoints.get(1);
        LngLat topRight = areaPoints.get(areaPoints.size() - 1);

        return (this.lat >= bottomLeft.lat && this.lat<= topRight.lat && this.lng >= bottomLeft.lng && this.lng <= topRight.lng);
    }

    /**
     * Determines the distance between two locations
     * @param other The point to which the distance is being measured
     * @return The distance, in degrees, between this point and the other one
     */
    public double distanceTo(LngLat other) {
        return Math.sqrt(((lng - other.lng) * (lng - other.lng)) +
                ((lat - other.lat) * (lat - other.lat)));
    }

    /**
     * Determines whether one location is close to another (i.e. the distance is below a specified tolerance)
     * @param other The point which is checked for closeness
     * @return A boolean representing whether the points are close or not
     */
    public boolean closeTo(LngLat other) {
        return (this.distanceTo(other) < DIST_TOLERANCE);
    }

    /**
     * Determines the coordinates of a drone if it moves in a particular direction starting from this point
     * @param dir The direction that the drone moves in
     * @return The point representing where the drone will end up
     */
    public LngLat nextPosition(CompassDirection dir) {
        if (dir == null) { return this; }

        double angle = Math.toRadians(dir.ordinal() * (360f / CompassDirection.values().length));
        double yChange = Math.sin(angle) * DIST_TOLERANCE;
        double xChange = Math.cos(angle) * DIST_TOLERANCE;
        return new LngLat(this.lng + xChange, this.lat + yChange);
    }
}
