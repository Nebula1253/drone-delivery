package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    // constant representing minimum distance value
    public static final double DIST_TOLERANCE = 0.00015;

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

        // the enum values have been specifically arranged in an anti-clockwise manner so that the radian value can be calculated
        double angle = Math.toRadians(dir.ordinal() * (360f / CompassDirection.values().length));

        // here "x" is longitude and "y" is latitude
        double yChange = Math.sin(angle) * DIST_TOLERANCE;
        double xChange = Math.cos(angle) * DIST_TOLERANCE;
        return new LngLat(this.lng + xChange, this.lat + yChange);
    }
}
