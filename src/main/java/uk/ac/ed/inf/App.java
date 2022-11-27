package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;

public class App 
{
    public static Area centralArea;
    public static Area[] noFlyZones;
    private static ArrayList<Order> orders;
    private static Drone drone;

    public static void main( String[] args ) {
        String orderDate = args[0];
        DataManager.setBaseURL(args[1]);
        orders = DataManager.retrieveDataFromURL("orders/" + orderDate, new TypeReference<>(){});
        centralArea = new Area(DataManager.retrieveDataFromURL("centralArea", new TypeReference<ArrayList<LngLat>>(){}));
        noFlyZones = DataManager.retrieveDataFromURL("noFlyZones", new TypeReference<>(){});

        drone = new Drone(orders);

        drone.deliverOrders();

        double[][] coordinates = {{-3.1907182931900024,55.94519570234043},
                {-3.1906163692474365,55.94498241796357},{-3.1900262832641597,55.94507554227258},{-3.190133571624756,55.94529783810495},{-3.1907182931900024,55.94519570234043}};
        Area elsieEnglisQuadrangle = new Area(coordinates);

        double[][] coordinates2 = {{-3.190578818321228,55.94402412577528},
                {-3.1899887323379517,55.94284650540911},
                {-3.187097311019897,55.94328811724263},
                {-3.187682032585144,55.944477740393744},
                {-3.190578818321228,55.94402412577528}};
        Area gsquare = new Area(coordinates2);


        LngLat a = new LngLat(-3.1901694479569898, 55.94320217859992);
        LngLat b = new LngLat(-3.1900633819398116, 55.94330824461709);

        System.out.println(gsquare.lineIntersectsArea(a,b));
    }
}
