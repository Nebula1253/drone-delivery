package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.beans.ConstructorProperties;
import java.util.ArrayList;

@JsonIgnoreProperties("name")
public class Area {
    private final ArrayList<LngLat> cornerPoints;

    public Area(ArrayList<LngLat> cornerPoints) {
        this.cornerPoints = cornerPoints;
    }

    @ConstructorProperties("coordinates")
    public Area(double[][] coordinates) {
        this.cornerPoints = new ArrayList<>();
        for (double[] i : coordinates) {
            cornerPoints.add(new LngLat(i[0], i[1]));
        }
    }

    public boolean pointInArea(LngLat point) {
        boolean odd = false;
        LngLat prevPt = cornerPoints.get(cornerPoints.size() - 1);

        for (LngLat currPt: cornerPoints) {
            // if line intersects this edge, invert "odd"
            if ((currPt.lat() > point.lat()) != (prevPt.lat() > point.lat())) { // the y-coordinate of this point lies between the y-coordinates of the edge points
                var xCoordIntersect = prevPt.lng() + ((currPt.lng() - prevPt.lng() * (point.lat() - prevPt.lat()) / (currPt.lat() - prevPt.lat())));
                if (point.lng() < xCoordIntersect) {
                    odd = !odd;
                    System.out.println(prevPt + " " + currPt);
                    System.out.println((prevPt.lng() - currPt.lng()) * (point.lat() - currPt.lat()) /
                            (prevPt.lat() - currPt.lat()) + currPt.lng());
                    System.out.println(point.lng());
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

    private int orientation(LngLat a, LngLat b, LngLat c) {
        var val = (b.lat() - a.lat()) * (c.lng() - b.lng()) - (b.lng() - a.lng()) * (c.lat() - b.lat());
        if (val == 0) return 0;
        return (val > 0) ? 1 : 2;
    }

    private boolean onSegment(LngLat a, LngLat b, LngLat c) {
        return (c.lng() <= Math.max(a.lng(), b.lng()) && c.lng() >= Math.min(a.lng(), b.lng()) &&
                c.lat() <= Math.max(a.lat(), b.lat()) && c.lat() >= Math.min(a.lat(), b.lat()));
    }

    public boolean lineIntersectsArea(LngLat lineStart, LngLat lineEnd) {
        LngLat edgeStart = cornerPoints.get(cornerPoints.size() - 1);

        for (LngLat edgeEnd : cornerPoints) {
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

            if (o1 == 0 && onSegment(lineStart, lineEnd, edgeStart)) return true;
            if (o2 == 0 && onSegment(lineStart, lineEnd, edgeEnd)) return true;
            if (o3 == 0 && onSegment(edgeStart, edgeEnd, lineStart)) return true;
            if (o4 == 0 && onSegment(edgeStart, edgeEnd, lineEnd)) return true;
            edgeStart = edgeEnd;
        }
        return false;
    }

    public ArrayList<LngLat> getCornerPoints() {
        return cornerPoints;
    }
}
