package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * So far this is pretty much a dumping ground for informal, disorganised tests relying on print statements
 */
public class App 
{
    private static final Area centralArea = new Area(DataManager.retrieveDataFromURL("centralArea", new TypeReference<ArrayList<LngLat>>(){}));
    private static final Area[] noFlyZones = DataManager.retrieveDataFromURL("noFlyZones", new TypeReference<>(){});
    private static Order[] orders;
    private static Drone drone;

    public static void main( String[] args ) {
        String orderDate = args[0];
        DataManager.setBaseURL(args[1]);
        orders = DataManager.retrieveDataFromURL("orders/" + orderDate, new TypeReference<Order[]>(){});
        drone = new Drone(orders);

        drone.deliverOrders();

        //DataManager.writeToJSONFile("test.json", "ksdjkofjef");
    }
}
