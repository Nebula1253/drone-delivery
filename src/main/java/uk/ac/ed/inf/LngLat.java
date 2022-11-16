package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
    private static final double DIST_TOLERANCE = 0.00015;

    /**
     * Checks whether this location is within the central area of the University campus, based on coordinates retrieved
     * from a REST server
     * @return A boolean value showing whether the point is within the central area
     * @throws IOException if the data retrieval fails
     */
    public boolean inCentralArea() throws IOException {
        ArrayList<LngLat> areaPoints = DataRetrieval.retrieveDataFromURL("centralArea", new TypeReference<>(){});

        // STRICTLY FOR TESTING NON-RECTANGULAR CENTRAL AREAS
        //areaPoints = changeAreaPoints();

        // Using the ray-casting method to check whether the point is inside the central area polygon i.e.
        // if a line from the point to the right toward infinity intersects the polygon edges an odd number of times or not
        // (odd means it's inside the polygon)

        boolean odd = false;
        LngLat prevPt = areaPoints.get(areaPoints.size() - 1);

        for (LngLat currPt: areaPoints) {
            // if line intersects this edge, invert "odd"
            if ((currPt.lat > this.lat) != (prevPt.lat > this.lat) && // the y-coordinate of this point lies between the y-coordinates of the edge points
                    (this.lng < (prevPt.lng - currPt.lng) * (this.lat - currPt.lat) /
                            (prevPt.lat - currPt.lat) + currPt.lng)) { // the x-coordinate of the intersection point is to the right of this point
                // for debugging
                //System.out.println(prevPt + ", " + currPt);
                odd = !odd;
            }

            // if the point is already on the edge, return true
            if (this.distanceTo(currPt) + this.distanceTo(prevPt) == currPt.distanceTo(prevPt)) {
                return true;
            }
            prevPt = currPt;
        }

        return odd;
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

        // the enum values have been specifically arranged in an anti-clockwise manner so that the radian value can be calculated
        double angle = Math.toRadians(dir.ordinal() * (360f / CompassDirection.values().length));

        // here "x" is longitude and "y" is latitude
        double yChange = Math.sin(angle) * DIST_TOLERANCE;
        double xChange = Math.cos(angle) * DIST_TOLERANCE;
        return new LngLat(this.lng + xChange, this.lat + yChange);
    }

    // STRICTLY FOR TESTING PURPOSES
    // terrible way of getting to test non-rectangular central area definitions
    private ArrayList<LngLat> changeAreaPoints() {
        // testing polygon 1
//        return new ArrayList<>(Arrays.asList(new LngLat(0,0), new LngLat(0, -5),
//                new LngLat(1, -5), new LngLat(1, -4), new LngLat(2, -4), new LngLat(3, -3),
//                new LngLat(1,1)));

        // testing polygon 2 (specifically for if the line passes straight through a vertex)
        return new ArrayList<>(Arrays.asList(new LngLat(0, 0), new LngLat(-2, -2),
                new LngLat(-1, -2), new LngLat(0, -1), new LngLat(1, -2),
                new LngLat(2, -2)));
    }
}
