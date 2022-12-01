package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.beans.ConstructorProperties;
import java.util.ArrayList;

/**
 * Represents an area enclosed by boundary points on the map
 */
@JsonIgnoreProperties("name")
public class Area {
    private final ArrayList<LngLat> cornerPoints;

    /**
     * Creates an area enclosed by boundary points on the map
     * @param cornerPoints a list of the boundary points
     */
    // intended for use with central area
    public Area(ArrayList<LngLat> cornerPoints) {
        this.cornerPoints = cornerPoints;
    }

    /**
     * Creates an area enclosed by boundary points on the map
     * @param coordinates a 2D array of lat-long coordinates, each sub-array representing one boundary point
     */
    // intended for use with no-fly zones
    @ConstructorProperties("coordinates")
    public Area(double[][] coordinates) {
        this.cornerPoints = new ArrayList<>();
        for (double[] i : coordinates) {
            cornerPoints.add(new LngLat(i[0], i[1]));
        }
    }

    /**
     * Checks whether a given point, with its latitude and longitude coordinates, is within this area
     * @param point the point being checked
     * @return True if the point is inside, False otherwise
     */
    public boolean pointInArea(LngLat point) {
        boolean odd = false;
        LngLat prevPt = cornerPoints.get(cornerPoints.size() - 1);

        for (LngLat currPt: cornerPoints) {
            // if line intersects this edge, invert "odd"
            if ((currPt.lat() > point.lat()) != (prevPt.lat() > point.lat())) { // the y-coordinate of this point lies between the y-coordinates of the edge points
                var xCoordIntersect = currPt.lng() + ((prevPt.lng() - currPt.lng()) * (point.lat() - currPt.lat()) / (prevPt.lat() - currPt.lat()));
                if (point.lng() < xCoordIntersect) {
                    odd = !odd;
                }
            }

            // if the point is already on the edge, return true
            if (point.distanceTo(currPt) + point.distanceTo(prevPt) == currPt.distanceTo(prevPt)) {
                return true;
            }
            prevPt = currPt;
        }

        return odd;
    }

    // Checks whether the orientation of an ordered triplet of points is clockwise or counterclockwise
    // Returns 0 if the points are collinear, 1 if the points are clockwise, 2 if the points are counterclockwise
    private int orientation(LngLat a, LngLat b, LngLat c) {
        var val = (b.lat() - a.lat()) * (c.lng() - b.lng()) - (b.lng() - a.lng()) * (c.lat() - b.lat());
        if (val == 0) return 0;
        return (val > 0) ? 1 : 2;
    }

    /**
     * Checks if a straight line, represented by two points, intersects the boundaries of this area
     * @param lineStart the start point of the line
     * @param lineEnd the end point of the line
     * @return True if the line intersects the boundaries, False otherwise
     */
    public boolean lineIntersectsArea(LngLat lineStart, LngLat lineEnd) {
        // taken from geeksforgeeks.com
        LngLat edgeStart = cornerPoints.get(cornerPoints.size() - 1);

        for (LngLat edgeEnd : cornerPoints) {
            int o1 = orientation(lineStart, lineEnd, edgeStart);
            int o2 = orientation(lineStart, lineEnd, edgeEnd);
            int o3 = orientation(edgeStart, edgeEnd, lineStart);
            int o4 = orientation(edgeStart, edgeEnd, lineEnd);

            if (o1 != o2 && o3 != o4) return true;
            edgeStart = edgeEnd;
        }
        return false;
    }
}
