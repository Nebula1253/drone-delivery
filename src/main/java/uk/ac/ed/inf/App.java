package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;

/**
 * So far this is pretty much a dumping ground for informal, disorganised tests relying on print statements
 */
public class App 
{
    private static Area centralArea;
    private static Area[] noFlyZones;
    private static Order[] orders;
    private static Drone drone;
    private String[] dates;

    public App() throws IOException {


    }

    public static void main( String[] args ) throws IOException{
        String orderDate = args[0];
        DataRetrieval.setBaseURL(args[1]);
        centralArea = new Area(DataRetrieval.retrieveDataFromURL("centralArea", new TypeReference<ArrayList<LngLat>>(){}));
        noFlyZones = DataRetrieval.retrieveDataFromURL("noFlyZones", new TypeReference<>(){});
        orders = DataRetrieval.retrieveDataFromURL("orders/" + orderDate, new TypeReference<>(){});
        drone = new Drone(orders);
    }
}
