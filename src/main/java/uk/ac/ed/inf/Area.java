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
            if ((currPt.lat() > point.lat()) != (prevPt.lat() > point.lat()) && // the y-coordinate of this point lies between the y-coordinates of the edge points
                    (point.lng() < (prevPt.lng() - currPt.lng()) * (point.lat() - currPt.lat()) /
                            (prevPt.lat() - currPt.lat()) + currPt.lng())) { // the x-coordinate of the intersection point is to the right of this point
                odd = !odd;
            }

            // if the point is already on the edge, return true
            if (point.distanceTo(currPt) + point.distanceTo(prevPt) == currPt.distanceTo(prevPt)) {
                return true;
            }
            prevPt = currPt;
        }

        return odd;
    }

    public boolean lineIntersectsArea(LngLat a, LngLat b) {
        // TODO: pretty sure this assumes the edge extends to infinity
        for (int i = 0; i < cornerPoints.size(); i++) {
            LngLat p1 = cornerPoints.get(i);
            LngLat p2;
            if (i == cornerPoints.size() - 1) {
                p2 = cornerPoints.get(0);
            }
            else p2 = cornerPoints.get(i+1);

            double gradient = (p2.lat() - p1.lat()) / (p2.lng() - p1.lng());
            double intercept = p2.lat() - (p2.lng() * gradient);

            double aVal = (gradient * a.lng()) + intercept - a.lat();
            double bVal = (gradient * b.lng()) + intercept - b.lat();

            if (Math.signum(aVal) != Math.signum(bVal)) return true;
        }
        return false;
    }

    public ArrayList<LngLat> getCornerPoints() {
        return cornerPoints;
    }
}
