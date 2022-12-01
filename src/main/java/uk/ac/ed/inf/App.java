package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * The main class of the drone service application, all code is executed from here and program-wide constants are defined here
 */
public class App 
{
    // The central area of campus, as defined on the REST server
    private static Area centralArea;

    // Areas that the drone is not allowed to fly over, as defined on the REST server
    private static Area[] noFlyZones;

    // The orders that the drone must deliver on this day
    private static ArrayList<Order> ordersForThisDay;

    // The current date on which the drone is delivering orders
    private static String orderDate;

    // The participating restaurants in the drone delivery service, as stored on the REST server
    private static Restaurant[] restaurants;

    /** The coordinates for Appleton Tower, the location where the drone will be delivering all the orders */
    public static final LngLat APPLETON_TOWER = new LngLat(-3.186874, 55.944494);


    /**
     * The main method, from which all other code is executed
     * @param args The command line arguments: the first argument is the desired date for which to deliver orders, the second is the server base URL,
     *             and the third is a seed for random-number-generation (unused in this implementation)
     */
    public static void main(String[] args) {
        orderDate = args[0];
        DataManager.setBaseURL(args[1]);

        restaurants = DataManager.retrieveDataFromURL("restaurants", new TypeReference<>(){});
        if (restaurants == null) throw new RuntimeException("Unable to retrieve restaurants from server");

        var centralAreaCornerPoints = DataManager.retrieveDataFromURL("centralArea", new TypeReference<ArrayList<LngLat>>(){});
        if (centralAreaCornerPoints == null) throw new RuntimeException("Unable to retrieve central area from server");
        centralArea = new Area(centralAreaCornerPoints);

        noFlyZones = DataManager.retrieveDataFromURL("noFlyZones", new TypeReference<>(){});
        if (noFlyZones == null) throw new RuntimeException("Unable to retrieve no-fly zones from server");

        ordersForThisDay = DataManager.retrieveDataFromURL("orders/" + orderDate, new TypeReference<>(){});
        if (ordersForThisDay == null) throw new RuntimeException("Unable to retrieve orders from server");

        int nrValidOrders = 0;
        for (Order order : ordersForThisDay) {
            if (order.getOutcome() == OrderOutcome.VALID_BUT_NOT_DELIVERED) {
                nrValidOrders++;
            }
        }
        System.out.println(nrValidOrders);

        // because of the default value of the order location (i.e. the value that all invalid orders will have)
        // being entirely outside Edinburgh, the distance from Appleton is obviously higher for those orders
        // therefore, valid orders are guaranteed to be at the start of this list
        ordersForThisDay.sort(Comparator.comparingDouble(o -> o.getPickupLocation().distanceTo(APPLETON_TOWER)));

        Drone drone = new Drone();
        drone.deliverOrders();

        DataManager.writeToJSONFile("deliveries-" + orderDate + ".json", ordersForThisDay);
    }

    // strictly for testing
    private void printNrOrdersWithOutcome() {
        Order[] allOrders = DataManager.retrieveDataFromURL("orders", new TypeReference<>(){});
        HashMap<OrderOutcome, Integer> map = new HashMap<>();

        assert allOrders != null;
        for (Order order : allOrders) {
            //if (order.getOutcome() == OrderOutcome.ValidButNotDelivered) validOrders++;
            map.put(order.getOutcome(), map.getOrDefault(order.getOutcome(), 0) + 1);
        }
        for (OrderOutcome o : map.keySet()) {
            System.out.println(o + " " + map.get(o));
        }
    }

    // getters for the values that remain constant through one execution of the app,
    // so that the other classes can't accidentally modify those values
    /**
     * @return Information on all the restaurants participating in the drone delivery service
     */
    public static Restaurant[] getRestaurants() {
        return restaurants;
    }

    /**
     * @return The central area of campus, as defined on the REST server
     */
    public static Area getCentralArea() {
        return centralArea;
    }

    /**
     * @return Areas that the drone is not allowed to fly over, as defined on the REST server
     */
    public static Area[] getNoFlyZones() {
        return noFlyZones;
    }

    /**
     * @return  The current date on which the drone is delivering orders
     */
    public static String getOrderDate() {
        return orderDate;
    }

    /**
     * @return The orders that the drone must deliver on this day
     */
    public static ArrayList<Order> getOrdersForThisDay() {
        return ordersForThisDay;
    }
}
