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
     * @param cornerPoints a list of LngLats representing the boundary points
     */
    // intended for use with central area
    public Area(ArrayList<LngLat> cornerPoints) {
        this.cornerPoints = cornerPoints;
    }

    /**
     * Creates an area enclosed by boundary points on the map
     * @param coordinates a 2D array of doubles, each sub-array representing one boundary point
     */
    // intended for use with no fly zones
    @ConstructorProperties("coordinates")
    public Area(double[][] coordinates) {
        this.cornerPoints = new ArrayList<>();
        for (double[] i : coordinates) {
            cornerPoints.add(new LngLat(i[0], i[1]));
        }
    }

    /**
     * Checks whether a given point, represented by latitude and longitude coordinates, is within this area
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
//                    System.out.println(prevPt + " " + currPt);
//                    System.out.println((prevPt.lng() - currPt.lng()) * (point.lat() - currPt.lat()) /
//                            (prevPt.lat() - currPt.lat()) + currPt.lng());
//                    System.out.println(point.lng());
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

    /**
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    private int orientation(LngLat a, LngLat b, LngLat c) {
        var val = (b.lat() - a.lat()) * (c.lng() - b.lng()) - (b.lng() - a.lng()) * (c.lat() - b.lat());
        if (val == 0) return 0;
        return (val > 0) ? 1 : 2;
    }

    /**
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    private boolean onSegment(LngLat a, LngLat b, LngLat c) {
        return (c.lng() <= Math.max(a.lng(), b.lng()) && c.lng() >= Math.min(a.lng(), b.lng()) &&
                c.lat() <= Math.max(a.lat(), b.lat()) && c.lat() >= Math.min(a.lat(), b.lat()));
    }

    /**
     * Checks if a straight line, represented by two points, intersects the boundaries of this area
     * @param lineStart the start point of the line
     * @param lineEnd the end point of the line
     * @return True if the
     */
    public boolean lineIntersectsArea(LngLat lineStart, LngLat lineEnd) {
        // taken from geeksforgeeks.com
        LngLat edgeStart = cornerPoints.get(cornerPoints.size() - 1);

        for (LngLat edgeEnd : cornerPoints) {
            // taken from geeksforgeeks.com
//            int o1 = orientation(p1, q1, p2);
//            int o2 = orientation(p1, q1, q2);
//            int o3 = orientation(p2, q2, p1);
//            int o4 = orientation(p2, q2, q1);
            int o1 = orientation(lineStart, lineEnd, edgeStart);
            int o2 = orientation(lineStart, lineEnd, edgeEnd);
            int o3 = orientation(edgeStart, edgeEnd, lineStart);
            int o4 = orientation(edgeStart, edgeEnd, lineEnd);

            if (o1 != o2 && o3 != o4) return true;


//            // p1, q1 and p2 are collinear and p2 lies on segment p1q1
//            if (o1 == 0 && onSegment(p1, q1, p2)) return true;
//
//            // p1, q1 and q2 are collinear and q2 lies on segment p1q1
//            if (o2 == 0 && onSegment(p1, q1, q2)) return true;
//
//            // p2, q2 and p1 are collinear and p1 lies on segment p2q2
//            if (o3 == 0 && onSegment(p2, q2, p1)) return true;
//
            // p2, q2 and q1 are collinear and q1 lies on segment p2q2
            //if (o4 == 0 && onSegment(p2, q2, q1)) return true;

//            if (o1 == 0 && onSegment(lineStart, lineEnd, edgeStart)) return true;
//            if (o2 == 0 && onSegment(lineStart, lineEnd, edgeEnd)) return true;
//            if (o3 == 0 && onSegment(edgeStart, edgeEnd, lineStart)) return true;
//            if (o4 == 0 && onSegment(edgeStart, edgeEnd, lineEnd)) return true;
            edgeStart = edgeEnd;
        }
        return false;
    }

    public boolean altLineIntersectsArea(LngLat moveStart, LngLat moveEnd) {
        var moveGradient = (moveEnd.lat() - moveStart.lat()) / (moveEnd.lng() - moveStart.lng());
        LngLat edgeStart = cornerPoints.get(cornerPoints.size() - 1);

        for (LngLat edgeEnd : cornerPoints) {
            var edgeGradient = (edgeEnd.lat() - edgeStart.lat()) / (edgeEnd.lng() - edgeStart.lng());
            if (moveGradient == 0 && edgeGradient == 0) {
                if (moveStart.lat() == edgeStart.lat()) {
                    return ((moveStart.lng() > edgeStart.lng()) != (moveStart.lng() > edgeEnd.lng()) || (moveEnd.lng() > edgeStart.lng()) != (moveEnd.lng() > edgeEnd.lng()));
                }
                else return false;
            }
            else if (Double.isInfinite(moveGradient) && Double.isInfinite(edgeGradient)) {
                if (moveStart.lng() == edgeStart.lng()) {
                    return ((moveStart.lat() >= edgeStart.lat()) != (moveStart.lat() >= edgeEnd.lat()) || (moveEnd.lat() >= edgeStart.lat()) != (moveEnd.lat() >= edgeEnd.lat()));
                }
                else return false;
            }
            else if (Double.isInfinite(edgeGradient) && Double.isFinite(moveGradient)) {
                double intercept;
                if (moveGradient == 0)  intercept = moveStart.lng();
                else intercept = moveStart.lat() - (moveGradient * moveStart.lng());

                var yCoordIntersect = (edgeStart.lng() * moveGradient) + intercept;

                if ((yCoordIntersect >= edgeStart.lat()) != (yCoordIntersect >= edgeEnd.lat())) {
                    return ((moveStart.lat() >= edgeStart.lat()) != (moveStart.lat() >= edgeEnd.lat()) || (moveEnd.lat() >= edgeStart.lat()) != (moveEnd.lat() >= edgeEnd.lat()));
                }
                else return false;
            }
            else if (Double.isFinite(edgeGradient) && Double.isInfinite(moveGradient)) {
                double intercept;
                if (edgeGradient == 0)  intercept = edgeStart.lng();
                else intercept = edgeStart.lat() - (edgeGradient * edgeStart.lng());

                var yCoordIntersect = (moveStart.lng() * edgeGradient) + intercept;

                if ((yCoordIntersect >= moveStart.lat()) != (yCoordIntersect >= moveEnd.lat())) {
                    return ((edgeStart.lat() >= moveStart.lat()) != (edgeStart.lat() >= moveEnd.lat()) || (edgeEnd.lat() >= moveStart.lat()) != (edgeEnd.lat() >= moveEnd.lat()));
                }
                else return false;
            }
            else if (edgeGradient == 0 && moveGradient > 0) {

            }

        }
        return false;
    }

    /**
     * Getter function for the vertices of this area
     * @return the area's vertices
     * */
    public ArrayList<LngLat> getCornerPoints() {
        return cornerPoints;
    }
}
