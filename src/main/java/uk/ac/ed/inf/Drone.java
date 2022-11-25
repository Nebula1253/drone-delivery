package uk.ac.ed.inf;

import java.util.*;

public class Drone {
    private ArrayList<Order> ordersToDeliver = new ArrayList<>();
    private final LngLat appletonTower = new LngLat(-3.186874, 55.944494);
    private int nr_moves = 2000;
    private String orderDate;

    public Drone(Order[] ordersToDeliver) {
        for (Order order : ordersToDeliver) {
            if (order.getOutcome() == OrderOutcome.ValidButNotDelivered) {
                this.ordersToDeliver.add(order);
            }
        }
        //this.ordersToDeliver.sort((o1, o2) -> Double.compare(o2.getDeliveryLocation().distanceTo(appletonTower), o1.getDeliveryLocation().distanceTo(appletonTower)));
        this.orderDate = this.ordersToDeliver.get(0).getOrderDate();
        DataManager.writeToJSONFile("ordertest.json", this.ordersToDeliver);
        System.out.println(this.ordersToDeliver.size());
    }

    public void deliverOrders() {
        ArrayList<LngLat> flightPath = new ArrayList<>();
        ArrayList<LngLat> currentOrderFlightPath;
        while (ordersToDeliver.size() > 0 && nr_moves > 0)  {
            // moves required for this specific order, for the drone to fly to restaurant and back to appleton tower
            Order currentOrd = ordersToDeliver.remove(0);
            currentOrderFlightPath = greedy(appletonTower, currentOrd.getDeliveryLocation());
            currentOrderFlightPath.addAll(greedy(currentOrd.getDeliveryLocation(), appletonTower));
            //System.out.println(currentOrderFlightPath.size());
            if (nr_moves >= currentOrderFlightPath.size()) {
                // update number of moves remaining
                nr_moves -= currentOrderFlightPath.size();
                flightPath.addAll(currentOrderFlightPath);
            }
            else break;
        }

        DataManager.writeToGeoJSONFile("drone-" + orderDate + ".geojson", flightPath);
        //DataManager.writeToJSONFile("deliveries-" + orderDate + ".json", ordersToDeliver);
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
                    minDist = neighbour.distanceTo(goal);
                    current = neighbour;
                }
            }
            flightPath.add(current);
        }
        return flightPath;
    }
}
