package uk.ac.ed.inf;

/**
 * Records one flight move performed by the drone
 * @param orderNo The eight-character order number for the pizza order which the drone is currently collecting or delivering
 * @param fromLongitude The longitude of the drone at the start of the move
 * @param fromLatitude The latitude of the drone at the start of the move
 * @param angle The angle of travel of the drone in this move, in radians
 * @param toLongitude The longitude of the drone at the end of this move
 * @param toLatitude The latitude of the drone at the end of this move
 * @param ticksSinceStartOfCalculation The elapsed ticks since the computation started for the day
 */
// pretty much exclusively intended for serialisation into the "flightpath" result files, absolutely useless outside that
public record DroneMove(String orderNo, double fromLongitude, double fromLatitude, Double angle,
                        double toLongitude, double toLatitude, int ticksSinceStartOfCalculation) {
}
