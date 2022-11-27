package uk.ac.ed.inf;

import java.util.*;

public class Drone {
    private final ArrayList<Order> ordersToDeliver;
    private static final LngLat APPLETON_TOWER = new LngLat(-3.186874, 55.944494);
    private int nrMoves = 2000;
    private int ticksSinceStartOfCalculation = 0;
    private String orderDate;
    private String orderNo;
    private ArrayList<DroneMove> moveLog = new ArrayList<>();

    public Drone(ArrayList<Order> ordersToDeliver) {
        int nrValidOrders = 0;
        for (Order order : ordersToDeliver) {
            if (order.getOutcome() == OrderOutcome.ValidButNotDelivered) {
                nrValidOrders++;
            }
        }
        System.out.println(nrValidOrders);

        // because of the default value of the order location (i.e. the value that all invalid orders will have)
        // being entirely outside Edinburgh, the distance from Appleton is obviously higher for those orders
        // therefore, valid orders are guaranteed to be at the start of this list
        this.ordersToDeliver = ordersToDeliver;
        this.ordersToDeliver.sort(Comparator.comparingDouble(o -> o.getDeliveryLocation().distanceTo(APPLETON_TOWER)));

        this.orderDate = this.ordersToDeliver.get(0).getOrderDate();
        this.orderNo = this.ordersToDeliver.get(0).getOrderNo();
    }

    public void deliverOrders() {
        ArrayList<LngLat> flightPath = new ArrayList<>();
        flightPath.add(APPLETON_TOWER);

        ArrayList<LngLat> currentOrderFlightPath;
        int ordersDelivered = 0;

        while (ordersToDeliver.size() > 0 && nrMoves > 0)  {
            // moves required for this specific order, for the drone to fly to restaurant and back to Appleton
            Order currentOrd = this.ordersToDeliver.get(ordersDelivered);
            if (currentOrd.getOutcome() != OrderOutcome.ValidButNotDelivered) break;

            currentOrderFlightPath = greedy(flightPath.get(flightPath.size() - 1) , currentOrd.getDeliveryLocation());
            currentOrderFlightPath.addAll(greedy(currentOrderFlightPath.get(currentOrderFlightPath.size() -1), APPLETON_TOWER));
//            for (int i = currentOrderFlightPath.size() - 1; i >= 0; i--) {
//                currentOrderFlightPath.add(currentOrderFlightPath.get(i));
//            }

            if (nrMoves >= currentOrderFlightPath.size()) {
                // update number of moves remaining
                nrMoves -= currentOrderFlightPath.size();

                // add to flightPath: this means the drone has "executed" the calculated path
                flightPath.addAll(currentOrderFlightPath);

                currentOrd.deliver();

                ordersDelivered++;
            }
            else {
                break;
            }
        }

        DataManager.writeToGeoJSONFile("drone-" + orderDate + ".geojson", flightPath);
        DataManager.writeToJSONFile("deliveries-" + orderDate + ".json", ordersToDeliver);

        System.out.println(ordersDelivered);
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
        //TODO: centralArea functionality: once it enters it can't exit
        LngLat current = start;
        ArrayList<LngLat> flightPath = new ArrayList<>();

        while (!current.closeTo(goal)) {
            var timeAtStart = (int) System.nanoTime();
            var minDistanceFromGoal = Double.POSITIVE_INFINITY;
            CompassDirection bestDir = null;

            for (CompassDirection dir : CompassDirection.values()) {
                LngLat neighbour = current.nextPosition(dir);
                boolean inNoFlyZone = false;

                // TODO: noFlyZone INTERSECTION (i.e. the LngLat itself is not within the zone, but the line between the current and destination node is)
                for (Area zone : App.noFlyZones) {
                    if (zone.pointInArea(neighbour) || zone.lineIntersectsArea(current, neighbour)) {
                        inNoFlyZone = true;
                        //distance += Double.POSITIVE_INFINITY;
//                            System.out.println(zone.getCornerPoints());
                        //System.out.println(current + " " + neighbour);
                        break;
                    }
                }
                if (inNoFlyZone || flightPath.contains(neighbour)) continue;
                //System.out.println(dir + " " + distance);
                //double distance = neighbour.distanceTo(goal);
                var distance = Math.abs(neighbour.lng() - goal.lng()) + Math.abs(neighbour.lat() - goal.lat());
                if (distance <= minDistanceFromGoal) {
                    minDistanceFromGoal = distance;
                    bestDir = dir;
                }
            }
            System.out.println(bestDir);
            //System.out.println(current);
            var nextPos = current.nextPosition(bestDir);

            var timeElapsed = (int) System.nanoTime() - timeAtStart;
            ticksSinceStartOfCalculation += timeElapsed;
            moveLog.add(new DroneMove(orderNo, current.lng(), current.lat(),
                    Math.toRadians(bestDir.ordinal() * (360f / CompassDirection.values().length)),
                    nextPos.lng(), nextPos.lat(), ticksSinceStartOfCalculation));

            current = nextPos;
            //TODO: add functionality to write drone move with 'from' and 'to' LngLats, as well as what angle was used

            flightPath.add(current);
        }
        return flightPath;
    }
}
