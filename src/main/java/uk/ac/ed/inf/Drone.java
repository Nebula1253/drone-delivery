package uk.ac.ed.inf;

import java.util.*;

/**
 * Represents one drone delivering orders in the drone delivery scheme
 */
public class Drone {
    private int nrMoves = 2000; // this one's self-explanatory
    private int ticksSinceStartOfCalculation = 0; // added to every time a move is calculated, to fulfil the "strictly increasing" constraint
    private final ArrayList<DroneMove> moveLog = new ArrayList<>(); // records all the moves, used in "flightpath" outfile
    private final ArrayList<LngLat> positionLog = new ArrayList<>(); // records all the positions, used in "drone" outfile

    /**
     * Makes the drone begin to deliver orders, outputs to files after execution
     */
    public void deliverOrders() {
        positionLog.add(App.APPLETON_TOWER);

        ArrayList<DroneMove> currentOrderFlightPath;
        String orderNo;
        Order currentOrd;
        int ordersDelivered = 0;

        while (nrMoves >= 0)  {
            // moves required for this specific order, for the drone to fly to restaurant and back to Appleton
            currentOrd = App.getOrdersForThisDay().get(ordersDelivered);
            if (currentOrd.getOutcome() != OrderOutcome.VALID_BUT_NOT_DELIVERED) {
                if (currentOrd.getOutcome() == OrderOutcome.DELIVERED) continue;
                else break;
            }
            orderNo = currentOrd.getOrderNo();

            // path from Appleton to restaurant and back is calculated before "execution"
            currentOrderFlightPath = greedy(positionLog.get(positionLog.size() - 1) , currentOrd.getPickupLocation(), orderNo);

            // reversing the path we've already got is way more efficient than calling the pathfinder twice
            currentOrderFlightPath.addAll(reverseDroneMoves(currentOrderFlightPath));

            // checks if the drone actually has the moves left to complete this order, breaks otherwise
            if (nrMoves >= currentOrderFlightPath.size()) {
                // update number of moves remaining
                nrMoves -= currentOrderFlightPath.size();

                // add to move and position logs: this means the drone has "executed" the calculated path
                moveLog.addAll(currentOrderFlightPath);

                for (DroneMove move : currentOrderFlightPath) {
                    // skips over hover moves so that the final location of the drone isn't included twice
                    if (move.angle() != null) positionLog.add(move.getEndLocation());
                }

                currentOrd.deliver();

                ordersDelivered++;
            }
            else break;
        }

        // write to files
        DataManager.writeToGeoJSONFile("drone-" + App.getOrderDate() + ".geojson", positionLog);
        DataManager.writeToJSONFile("flightpath-" + App.getOrderDate() + ".json", moveLog);

        // just to check score
        System.out.println(ordersDelivered);
        // the flight path size should be one greater than the move log size, because it starts with AT whereas the move log starts with the first actual move
        System.out.println(positionLog.size() + " " + moveLog.size());
    }

    // Greedy pathfinding algorithm for drone flight, returns a list of coordinates (LngLats) representing the positions of the drone through the path
    private ArrayList<DroneMove> greedy(LngLat start, LngLat goal, String orderNo) {
        LngLat current = start;
        ArrayList<DroneMove> flightPath = new ArrayList<>();

        // flag to allow the algorithm to obey the constraint of "once you've re-entered the central area you can't exit it again"
        boolean reenteredCentralArea = false;

        // repeat until the drone has reached its destination
        while (!current.closeTo(goal)) {
            // used to measure the time this move calculation took
            var timeAtStartOfCalculation = System.nanoTime();

            // initialising distance and direction values
            var minDistanceFromGoal = Double.POSITIVE_INFINITY;
            CompassDirection bestDir = CompassDirection.EAST;

            // iterates through all directions, trying to find the one that takes us closest to our goal
            for (CompassDirection dir : CompassDirection.values()) {
                LngLat neighbour = current.nextPosition(dir);
                boolean inNoFlyZone = false;

                // checks if moving to this point takes us through any of the no-fly zones
                for (Area zone : App.getNoFlyZones()) {
                    if (zone.pointInArea(neighbour) || zone.lineIntersectsArea(current, neighbour)) {
                        inNoFlyZone = true;
                        break;
                    }
                }
                // if the potential direction we're looking at takes us through a no-fly zone,
                // or exits the central area once we've re-entered it, don't consider it
                if (inNoFlyZone ||
                        (reenteredCentralArea && !App.getCentralArea().pointInArea(neighbour)) ||
                        (!App.getCentralArea().pointInArea(current) && !App.getCentralArea().pointInArea(neighbour) && App.getCentralArea().lineIntersectsArea(current, neighbour)))
                    continue;

                // checks if this is the ideal direction
                double distance = neighbour.distanceTo(goal);
                if (distance <= minDistanceFromGoal) {
                    minDistanceFromGoal = distance;
                    bestDir = dir;
                }
            }

            // the best next position calculated
            var nextPos = current.nextPosition(bestDir);

            // logs drone move
            var timeAtEndOfCalculation = System.nanoTime() - timeAtStartOfCalculation;
            ticksSinceStartOfCalculation += Math.ceil(timeAtEndOfCalculation);
            flightPath.add(new DroneMove(orderNo, current.lng(), current.lat(),
                    Math.toRadians(bestDir.ordinal() * (360f / CompassDirection.values().length)),
                    nextPos.lng(), nextPos.lat(), ticksSinceStartOfCalculation));

            // if we weren't in the central area and will now be in it, set the flag so that we know to exclude paths outside the central area
            if (!App.getCentralArea().pointInArea(current) && App.getCentralArea().pointInArea(nextPos)) reenteredCentralArea = true;

            current = nextPos;
        }

        // hover move once we've reached destination
        ticksSinceStartOfCalculation++;
        flightPath.add(new DroneMove(orderNo, current.lng(), current.lat(),
                null, current.lng(), current.lat(), ticksSinceStartOfCalculation));

        return flightPath;
    }

    // used for moves required to get the drone back to Appleton
    private ArrayList<DroneMove> reverseDroneMoves(ArrayList<DroneMove> movesToReverse) {
        ArrayList<DroneMove> reversedPath = new ArrayList<>();
        DroneMove move = movesToReverse.get(0);
        String orderNo = move.orderNo();

        // need to exclude the last hover move, so we start from the second-to-last entry instead
        for (int i = movesToReverse.size() - 2; i >= 0; i--) {
            move = movesToReverse.get(i);

            // ensure the angle value is accurate; needs to be in the opposite direction of whatever the original was
            double reversedAngle;
            if (move.angle() >= Math.PI) reversedAngle = move.angle() - Math.PI;
            else reversedAngle = move.angle() + Math.PI;

            // to obey the "strictly increasing" constraint on the ticks field
            ticksSinceStartOfCalculation++;
            reversedPath.add(new DroneMove(orderNo, move.toLongitude(), move.toLatitude(), reversedAngle,
                    move.fromLongitude(), move.fromLatitude(), ticksSinceStartOfCalculation));
        }

        // hover move
        ticksSinceStartOfCalculation++;
        reversedPath.add(new DroneMove(orderNo, move.fromLongitude(), move.fromLatitude(), null, move.fromLongitude(), move.fromLatitude(), ticksSinceStartOfCalculation));
        return reversedPath;
    }
}
