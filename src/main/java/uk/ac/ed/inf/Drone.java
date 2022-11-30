package uk.ac.ed.inf;

import java.util.*;

/**
 * Represents one drone delivering orders in the drone delivery scheme
 */
public class Drone {
    private int nrMoves = 2000;
    private int ticksSinceStartOfCalculation = 0;
    //private final String orderDate;
    private String orderNo;
    private final ArrayList<DroneMove> moveLog = new ArrayList<>();
    private final ArrayList<LngLat> flightPath = new ArrayList<>();

    public Drone() {
    }

    /**
     * Makes the drone begin to deliver orders, outputs to files after execution
     */
    public void deliverOrders() {
        flightPath.add(App.APPLETON_TOWER);

        ArrayList<LngLat> currentOrderFlightPath;
        int ordersDelivered = 0;

        while (nrMoves >= 0)  {
            // moves required for this specific order, for the drone to fly to restaurant and back to Appleton
            Order currentOrd = App.ordersForThisDay.get(ordersDelivered);
            if (currentOrd.getOutcome() != OrderOutcome.ValidButNotDelivered) {
                if (currentOrd.getOutcome() == OrderOutcome.Delivered) continue;
                else break;
            }
            this.orderNo = currentOrd.getOrderNo();

            // path from Appleton to restaurant and back is calculated before "execution"
            currentOrderFlightPath = greedy(flightPath.get(flightPath.size() - 1) , currentOrd.getPickupLocation());
            currentOrderFlightPath.addAll(greedy(currentOrderFlightPath.get(currentOrderFlightPath.size() -1), App.APPLETON_TOWER));
//            currentOrderFlightPath = A_star(flightPath.get(flightPath.size() - 1) , currentOrd.getDeliveryLocation());
            //currentOrderFlightPath.addAll(A_star(currentOrderFlightPath.get(currentOrderFlightPath.size() -1), App.APPLETON_TOWER));

            // checks if the drone actually has the moves left to complete this order, breaks otherwise
            if (nrMoves >= currentOrderFlightPath.size()) {
                // update number of moves remaining
                nrMoves -= currentOrderFlightPath.size();

                // add to flightPath: this means the drone has "executed" the calculated path
                flightPath.addAll(currentOrderFlightPath);

                currentOrd.deliver();

                ordersDelivered++;
            }
            else {
                // because the flight path is calculated before "execution", the moves are added before we know if the drone can fly them or not
                // in the case where the moves for the next order are added to moveLog, but the drone can't fulfill those moves, we need to remove them from the log
                int removed = 0;
                while (removed < currentOrderFlightPath.size()) {
                    moveLog.remove(moveLog.size() - 1);
                    removed++;
                }
                break;
            }
        }

        DataManager.writeToGeoJSONFile("drone-" + App.orderDate + ".geojson", flightPath);
        DataManager.writeToJSONFile("flightpath-" + App.orderDate + ".json", moveLog);

        System.out.println(ordersDelivered);
        // the flight path size should be one greater than the move log size, because it starts with AT whereas the move log starts with the first actual move
        System.out.println(flightPath.size() + " " + moveLog.size());
    }

    private ArrayList<LngLat> A_star(LngLat start, LngLat goal) {
        // heuristic function is distanceTo divided by move length, rounded
        HashMap<LngLat, LngLat> cameFrom = new HashMap<>();

        HashMap<LngLat, Integer> gScore = new HashMap<>();
        gScore.put(start, 0);

        HashMap<LngLat, Integer> fScore = new HashMap<>();
        fScore.put(start, (int) Math.ceil(start.distanceTo(goal) / LngLat.DIST_TOLERANCE));

        PriorityQueue<LngLat> openSet = new PriorityQueue<>((first, second) -> {
            Integer firstFScore = fScore.get(first);
            Integer secondFScore = fScore.get(second);

            if (firstFScore == null) { firstFScore = Integer.MAX_VALUE; }
            if (secondFScore == null) { secondFScore = Integer.MAX_VALUE; }

            return secondFScore.compareTo(firstFScore);
        });
        openSet.add(start);

        while (!openSet.isEmpty()) {
            LngLat current = openSet.remove();
            if (current.equals(goal)) {
                return reconstructPath(cameFrom, current);
            }

            for (CompassDirection dir : CompassDirection.values()) {
                LngLat neighbour = current.nextPosition(dir);

                int tentativeGScore = gScore.get(current) + 1;
                int comparisonGScore = gScore.getOrDefault(neighbour, Integer.MAX_VALUE);

                if (tentativeGScore < comparisonGScore) {
                    cameFrom.put(neighbour, current);
                    gScore.put(neighbour, tentativeGScore);
                    fScore.put(neighbour, tentativeGScore + (int) Math.ceil(neighbour.distanceTo(goal) / LngLat.DIST_TOLERANCE));
                    if (!openSet.contains(neighbour)) {
                        openSet.add(neighbour);
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private ArrayList<LngLat> reconstructPath(HashMap<LngLat, LngLat> cameFrom, LngLat current) {
        ArrayList<LngLat> totalPath = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.add(0, current);
        }
        return totalPath;
    }

    // Greedy pathfinding algorithm for drone flight, returns a list of coordinates (LngLats) representing the positions of the drone through the path
    private ArrayList<LngLat> greedy(LngLat start, LngLat goal) {
        LngLat current = start;
        ArrayList<LngLat> flightPath = new ArrayList<>();

        // flag to allow the algorithm to obey the constraint of "once you've re-entered the central area you can't exit it again"
        boolean reenteredCentralArea = false;

        // repeat until the drone has reached its destination
        while (!current.closeTo(goal)) {
            // used to measure the time this move calculation took
            var timeAtStartOfCalculation = (int) System.nanoTime();

            // initialising distance and direction values
            var minDistanceFromGoal = Double.POSITIVE_INFINITY;
            CompassDirection bestDir = CompassDirection.EAST;

            // iterates through all directions, trying to find the one that takes us closest to our goal
            for (CompassDirection dir : CompassDirection.values()) {
                LngLat neighbour = current.nextPosition(dir);
                boolean inNoFlyZone = false;

                // checks if moving to this point takes us through any of the no-fly zones
                for (Area zone : App.noFlyZones) {
                    if (zone.pointInArea(neighbour) || zone.lineIntersectsArea(current, neighbour)) {
                        inNoFlyZone = true;
                        break;
                    }
                }
                // if the potential direction we're looking at takes us through a no-fly zone, or exits the central area once we've re-entered it,
                // or we've already visited this point before, don't consider it
                if (inNoFlyZone || (reenteredCentralArea && !App.centralArea.pointInArea(neighbour)) || flightPath.contains(neighbour)) continue;

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
            var timeAtEndOfCalculation = (int) System.nanoTime() - timeAtStartOfCalculation;
            ticksSinceStartOfCalculation += timeAtEndOfCalculation;
            moveLog.add(new DroneMove(orderNo, current.lng(), current.lat(),
                    Math.toRadians(bestDir.ordinal() * (360f / CompassDirection.values().length)),
                    nextPos.lng(), nextPos.lat(), ticksSinceStartOfCalculation));

            // if we weren't in the central area and will now be in it, set the flag so that we know to exclude paths outside the central area
            if (!App.centralArea.pointInArea(current) && App.centralArea.pointInArea(nextPos)) reenteredCentralArea = true;

            // logs position
            current = nextPos;
            flightPath.add(current);
        }

        // hover move once we've reached destination
        flightPath.add(current);
        ticksSinceStartOfCalculation++;
        moveLog.add(new DroneMove(orderNo, current.lng(), current.lat(),
                null, current.lng(), current.lat(), ticksSinceStartOfCalculation));

        return flightPath;
    }
}
