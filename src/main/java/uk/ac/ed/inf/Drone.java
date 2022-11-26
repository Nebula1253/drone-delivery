package uk.ac.ed.inf;

import java.util.*;

public class Drone {
    private final ArrayList<Order> ordersToDeliver;
    private static final LngLat APPLETON_TOWER = new LngLat(-3.186874, 55.944494);
    private int nrMoves = 2000;
    private String orderDate;

    public Drone(ArrayList<Order> ordersToDeliver) {
        int nrValidOrders = 0;
        for (Order order : ordersToDeliver) {
            if (order.getOutcome() == OrderOutcome.ValidButNotDelivered) {
                nrValidOrders++;
            }
        }
        System.out.println(nrValidOrders);
        this.ordersToDeliver = ordersToDeliver;
        // because of the default value of the order location (i.e. the value that all invalid orders will have)
        // being entirely outside Edinburgh, the distance from Appleton is obviously higher for those orders
        // therefore, valid orders are guaranteed to be at the start of this list
        this.ordersToDeliver.sort(Comparator.comparingDouble(o -> o.getDeliveryLocation().distanceTo(APPLETON_TOWER)));
        this.orderDate = this.ordersToDeliver.get(0).getOrderDate();
        //DataManager.writeToJSONFile("ordertest.json", this.ordersToDeliver);
        //System.out.println(this.ordersToDeliver.size());
    }

    public void deliverOrders() {
        ArrayList<LngLat> flightPath = new ArrayList<>();
        flightPath.add(APPLETON_TOWER);

        ArrayList<LngLat> currentOrderFlightPath;
        ArrayList<Order> dummyOrders = (ArrayList<Order>) ordersToDeliver.clone();
        int ordersDelivered = 0;

        while (ordersToDeliver.size() > 0 && nrMoves > 0)  {
            // moves required for this specific order, for the drone to fly to restaurant and back to Appleton
            Order currentOrd = dummyOrders.remove(0);
            currentOrderFlightPath = greedy(flightPath.get(flightPath.size() - 1) , currentOrd.getDeliveryLocation());
            //currentOrderFlightPath.addAll(greedy(currentOrderFlightPath.get(currentOrderFlightPath.size() -1), APPLETON_TOWER));
            for (int i = currentOrderFlightPath.size() - 1; i >= 0; i--) {
                currentOrderFlightPath.add(currentOrderFlightPath.get(i));
            }

            if (nrMoves >= currentOrderFlightPath.size()) {
                // update number of moves remaining
                nrMoves -= currentOrderFlightPath.size();

                // add to flightPath: this means the drone has "executed" the calculated path
                flightPath.addAll(currentOrderFlightPath);

                ordersToDeliver.get(ordersDelivered).deliver();

                ordersDelivered++;
            }
            else break;
        }

        DataManager.writeToGeoJSONFile("drone-" + orderDate + ".geojson", flightPath);
        DataManager.writeToJSONFile("deliveries-" + orderDate + ".json", ordersToDeliver);
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

    private ArrayList<LngLat> greedy(LngLat start, LngLat goal) {
        LngLat current = start;
        ArrayList<LngLat> flightPath = new ArrayList<>();
        while (current.distanceTo(goal) > LngLat.DIST_TOLERANCE) {
            double minDist = Double.POSITIVE_INFINITY;
            for (CompassDirection dir : CompassDirection.values()) {
                LngLat neighbour = current.nextPosition(dir);
                if (neighbour.distanceTo(goal) < minDist) {
                    // TODO: noFlyZone INTERSECTION (i.e. the LngLat itself is not within the zone, but the line between the current and destination node is)
                    boolean inNoFlyZone = false;
                    for (Area zone : App.noFlyZones) {
                        if (zone.pointInArea(neighbour)) {
                            inNoFlyZone = true;
                            break;
                        }
                    }
                    if (!inNoFlyZone) {
                        minDist = neighbour.distanceTo(goal);
                        current = neighbour;
                    }
                }
            }
            //TODO: add functionality to write drone move with 'from' and 'to' LngLats, as well as what angle was used
            flightPath.add(current);
        }
        return flightPath;
    }
}
