package uk.ac.ed.inf;

import java.util.*;

public class Drone {
    private ArrayList<Order> ordersToDeliver;
    private final LngLat appletonTower = new LngLat(-3.186874, 55.944494);
    private int nr_moves = 2000;
    private String orderDate;

    public Drone(Order[] ordersToDeliver) {
        Arrays.sort(ordersToDeliver, (o1, o2) -> Double.compare(o2.getDeliveryLocation().distanceTo(appletonTower), o1.getDeliveryLocation().distanceTo(appletonTower)));
        this.ordersToDeliver = new ArrayList<>(List.of(ordersToDeliver));
        this.orderDate = this.ordersToDeliver.get(0).getOrderDate();
    }

    public void deliverOrders() {
        while (ordersToDeliver.size() > 0) {
            Order currentOrd = ordersToDeliver.remove(0);
            // deliver order

            A_star(appletonTower, currentOrd.getDeliveryLocation());
        }

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
}
