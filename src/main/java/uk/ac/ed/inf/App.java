package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class App 
{
    /** The central area of campus, as defined on the REST server*/
    public static Area centralArea;

    /** Areas that the drone is not allowed to fly over, as defined on the REST server*/
    public static Area[] noFlyZones;

    /** The orders that the drone must deliver on this day */
    public static ArrayList<Order> ordersForThisDay;

    /** The current date on which the drone is delivering orders */
    public static String orderDate;

    /** The coordinates for Appleton Tower, the location where the drone will be delivering all the orders */
    public static final LngLat APPLETON_TOWER = new LngLat(-3.186874, 55.944494);

    /** The participating restaurants in the drone delivery service, as stored on the REST server */
    public static Restaurant[] restaurants;


    public static void main(String[] args) {
        orderDate = args[0];
        DataManager.setBaseURL(args[1]);

        restaurants = DataManager.retrieveDataFromURL("restaurants", new TypeReference<>(){});
        ordersForThisDay = DataManager.retrieveDataFromURL("orders/" + orderDate, new TypeReference<>(){});
        if (ordersForThisDay == null) throw new RuntimeException("Unable to retrieve orders from server");

        int nrValidOrders = 0;
        for (Order order : ordersForThisDay) {
            if (order.getOutcome() == OrderOutcome.ValidButNotDelivered) {
                nrValidOrders++;
            }
        }
        System.out.println(nrValidOrders);

        var centralAreaCornerPoints = DataManager.retrieveDataFromURL("centralArea", new TypeReference<ArrayList<LngLat>>(){});
        if (centralAreaCornerPoints == null) throw new RuntimeException("Unable to retrieve central area from server");
        centralArea = new Area(centralAreaCornerPoints);

        noFlyZones = DataManager.retrieveDataFromURL("noFlyZones", new TypeReference<>(){});
        if (noFlyZones == null) throw new RuntimeException("Unable to retrieve no-fly zones from server");

        // because of the default value of the order location (i.e. the value that all invalid orders will have)
        // being entirely outside Edinburgh, the distance from Appleton is obviously higher for those orders
        // therefore, valid orders are guaranteed to be at the start of this list
        ordersForThisDay.sort(Comparator.comparingDouble(o -> o.getPickupLocation().distanceTo(APPLETON_TOWER)));

        Drone drone = new Drone();
        drone.deliverOrders();

        DataManager.writeToJSONFile("deliveries-" + orderDate + ".json", ordersForThisDay);

        //System.out.println(validOrders);

//        double[][] coordinates = {{-3.1907182931900024,55.94519570234043},
//                {-3.1906163692474365,55.94498241796357},{-3.1900262832641597,55.94507554227258},{-3.190133571624756,55.94529783810495},{-3.1907182931900024,55.94519570234043}};
//        Area elsieEnglisQuadrangle = new Area(coordinates);
//
//        double[][] coordinates2 = {{-3.190578818321228,55.94402412577528},
//                {-3.1899887323379517,55.94284650540911},
//                {-3.187097311019897,55.94328811724263},
//                {-3.187682032585144,55.944477740393744},
//                {-3.190578818321228,55.94402412577528}};
//        Area gsquare = new Area(coordinates2);
//
//
//        LngLat a = new LngLat(-3.1901694479569898, 55.94320217859992);
//        LngLat b = new LngLat(-3.1900633819398116, 55.94330824461709);
//
//        System.out.println(gsquare.lineIntersectsArea(a,b));
    }

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
}
