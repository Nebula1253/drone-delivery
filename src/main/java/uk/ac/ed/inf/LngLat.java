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
    // constant representing minimum distance value
    private static final double DIST_TOLERANCE = 0.00015;

    /**
     * Checks whether this location is within the central area of the University campus, based on coordinates retrieved
     * from a REST server
     * @return A boolean value showing whether the point is within the central area
     * @throws IOException if the data retrieval fails
     */
    public boolean inCentralArea() throws IOException {
        ArrayList<LngLat> areaPoints = CentralArea.getInstance().getCentralArea();

        // TODO: modify to account for n-sided polygons
//        LngLat bottomLeft = areaPoints.get(1);
//        LngLat topRight = areaPoints.get(areaPoints.size() - 1);
//
//        return (this.lat >= bottomLeft.lat && this.lat<= topRight.lat && this.lng >= bottomLeft.lng && this.lng <= topRight.lng);

        // Using the ray-casting method to check whether the point is inside the central area polygon i.e.
        // if a line from the point to the right toward infinity intersects the polygon edges an odd number of times or not
        // (odd means it's inside the polygon)

        boolean odd = false;
        int j = areaPoints.size() - 1;

        // For each edge (in this case, between the current point of the polygon and the previous one), starting with the edge
        // between the last and the first node
        for (int i = 0; i < areaPoints.size(); i++) {
            // Check if a line from the point into infinity crosses the current edge
            double gradient = (areaPoints.get(i).lat - areaPoints.get(j).lat) / (areaPoints.get(i).lng - areaPoints.get(j).lng);

            // if line is horizontal you're not getting an intersection anyway
            if (gradient == 0) {
                j = i;
                continue;
            }
            // if line is vertical, just check if the y coordinate lies in between the two line endpoints,
            // and if the x-coordinate is to the right
            else if (Double.isInfinite(gradient)) {
                if (((this.lat > areaPoints.get(i).lat) != (this.lat > areaPoints.get(j).lat)) &&
                        this.lng < areaPoints.get(i).lng) {
                    odd = !odd;
                }
            }
            else {
                double yIntercept = areaPoints.get(i).lat - (areaPoints.get(i).lng * gradient);

                // get x-coordinate of point of intersection between current edge and line from current point to infinity
                // which will obviously have equation (y = y-coordinate of current point)
                double xCoord = (this.lat - yIntercept) / gradient;

                // if the point of intersection is in fact to the *right* of the current point (i.e. the edge is to the right),
                // count the intersection
                if (xCoord > this.lng) {
                    odd = !odd;
                }
            }
            j = i;
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

        double angle = Math.toRadians(dir.ordinal() * (360f / CompassDirection.values().length));
        double yChange = Math.sin(angle) * DIST_TOLERANCE;
        double xChange = Math.cos(angle) * DIST_TOLERANCE;
        return new LngLat(this.lng + xChange, this.lat + yChange);
    }
}
