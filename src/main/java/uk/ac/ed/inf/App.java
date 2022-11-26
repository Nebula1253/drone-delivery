package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;

public class App 
{
    public static final Area centralArea = new Area(DataManager.retrieveDataFromURL("centralArea", new TypeReference<ArrayList<LngLat>>(){}));
    public static final Area[] noFlyZones = DataManager.retrieveDataFromURL("noFlyZones", new TypeReference<>(){});
    private static ArrayList<Order> orders;
    private static Drone drone;

    public static void main( String[] args ) {
        String orderDate = args[0];
        DataManager.setBaseURL(args[1]);
        orders = DataManager.retrieveDataFromURL("orders/" + orderDate, new TypeReference<>(){});
        drone = new Drone(orders);

        drone.deliverOrders();
    }
}
