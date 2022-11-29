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

    public Drone() {
        int nrValidOrders = 0;
        for (Order order : App.ordersForThisDay) {
            if (order.getOutcome() == OrderOutcome.ValidButNotDelivered) {
                nrValidOrders++;
            }
        }
        System.out.println(nrValidOrders);

        // because of the default value of the order location (i.e. the value that all invalid orders will have)
        // being entirely outside Edinburgh, the distance from Appleton is obviously higher for those orders
        // therefore, valid orders are guaranteed to be at the start of this list
        //App.ordersForThisDay.sort(Comparator.comparingDouble(o -> o.getDeliveryLocation().distanceTo(APPLETON_TOWER)));

        // since the app is only run once per day anyway,
        //this.orderDate = App.ordersForThisDay.get(0).getOrderDate();
    }

    /**
     * Makes the drone begin to deliver orders, outputs to files after execution
     */
    public void deliverOrders() {
        ArrayList<LngLat> flightPath = new ArrayList<>();
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
            currentOrderFlightPath = greedy(flightPath.get(flightPath.size() - 1) , currentOrd.getDeliveryLocation());
            currentOrderFlightPath.addAll(greedy(currentOrderFlightPath.get(currentOrderFlightPath.size() -1), App.APPLETON_TOWER));

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
                for (int i = 1; i < currentOrderFlightPath.size(); i++) {
                    moveLog.remove(moveLog.size() - i);
                }
                break;
            }
        }

        DataManager.writeToGeoJSONFile("drone-" + App.orderDate + ".geojson", flightPath);
        DataManager.writeToJSONFile("flightpath-" + App.orderDate + ".json", moveLog);

        System.out.println(ordersDelivered);
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

    /**
     * Greedy pathfinding algorithm for drone flight
     * @param start LngLat representing the start point of the drone
     * @param goal LngLat representing the end point of the drone
     * @return a list of coordinates (LngLats) representing the positions of the drone through the path
     */
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
            CompassDirection bestDir = null;

            // iterates through all directions, trying to find the one that takes us closest to our goal
            for (CompassDirection dir : CompassDirection.values()) {
                LngLat neighbour = current.nextPosition(dir);
                boolean inNoFlyZone = false;

                // checks if moving to this point takes us through any of the no-fly zones
                for (Area zone : App.NO_FLY_ZONES) {
                    if (zone.pointInArea(neighbour) || zone.lineIntersectsArea(current, neighbour)) {
                        inNoFlyZone = true;
                        break;
                    }
                }
                // if the potential direction we're looking at takes us through a no-fly zone, or exits the central area once we've re-entered it,
                // or we've already visited this point before, don't consider it
                if (inNoFlyZone || (reenteredCentralArea && !App.CENTRAL_AREA.pointInArea(neighbour)) || flightPath.contains(neighbour)) continue;

                // checks if this is the ideal direction
                double distance = neighbour.distanceTo(goal);
                if (distance <= minDistanceFromGoal) {
                    minDistanceFromGoal = distance;
                    bestDir = dir;
                }
            }
//            System.out.println(bestDir);
//            System.out.println(current);

            // the best next position calculated
            var nextPos = current.nextPosition(bestDir);

            // logs drone move
            var timeAtEndOfCalculation = (int) System.nanoTime() - timeAtStartOfCalculation;
            ticksSinceStartOfCalculation += timeAtEndOfCalculation;
            moveLog.add(new DroneMove(orderNo, current.lng(), current.lat(),
                    Math.toRadians(bestDir.ordinal() * (360f / CompassDirection.values().length)),
                    nextPos.lng(), nextPos.lat(), ticksSinceStartOfCalculation));

            // if we weren't in the central area and will now be in it, set the flag so we know to exclude paths outside the central area
            if (!App.CENTRAL_AREA.pointInArea(current) && App.CENTRAL_AREA.pointInArea(nextPos)) reenteredCentralArea = true;

            // logs position
            current = nextPos;
            flightPath.add(current);
        }

        // hover move once we've reached destination
        ticksSinceStartOfCalculation++;
        moveLog.add(new DroneMove(orderNo, current.lng(), current.lat(),
                null, current.lng(), current.lat(), ticksSinceStartOfCalculation));

        return flightPath;
    }
}
