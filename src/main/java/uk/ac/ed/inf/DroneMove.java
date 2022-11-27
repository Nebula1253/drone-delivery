package uk.ac.ed.inf;

/**
 *
 * @param orderNo
 * @param fromLongitude
 * @param fromLatitude
 * @param angle
 * @param toLongitude
 * @param toLatitude
 * @param ticksSinceStartOfCalculation
 */
public record DroneMove(String orderNo, double fromLongitude, double fromLatitude, double angle,
                        double toLongitude, double toLatitude, int ticksSinceStartOfCalculation) {
}
