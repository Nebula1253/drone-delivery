package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * So far this is pretty much a dumping ground for informal, disorganised tests relying on print statements
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InvalidPizzaCombinationException {
        // testing central area fetching from endpoint, lnglat names are self explanatory
        LngLat forSureInCentralArea = new LngLat(-3.19, 55.943);
        System.out.println(forSureInCentralArea);
        System.out.println(forSureInCentralArea.inCentralArea());

        LngLat noWayItsInThere = new LngLat(420, 69);
        System.out.println(noWayItsInThere.inCentralArea());

        LngLat absoluteBoundaryCase = new LngLat(-3.192473,55.946233);
        System.out.println(absoluteBoundaryCase.inCentralArea());

        System.out.println(noWayItsInThere.distanceTo(forSureInCentralArea));

        // testing closeTo and nextPosition
        System.out.println(forSureInCentralArea.closeTo(new LngLat(-3.190005, 55.9430005)));
        // expected true

        System.out.println(forSureInCentralArea.nextPosition(CompassDirection.NORTH));
        // pretty much just manually checking coordinates here

        // testing whether it correctly returns 4 restaurants
        var x = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net/"));
        System.out.println(Arrays.toString(x));

        // testing whether it correctly parsed each restaurant's menu
        for (Restaurant r : x) {
            System.out.println(Arrays.toString(r.getMenu()));
        }

        // testing whether it returns the correct delivery cost
        Order order = new Order("12", "420-69-1337", "Cust O. Mer", "1234567890", "09-26",
                "576", 0, new ArrayList<>());

        System.out.println(order.getDeliveryCost(x, "Vegan Delight", "Meat Lover"));

        // specifically for testing non-rectangular polygon cases
        // ONLY MAKES SENSE WITH 1ST NEW TESTING POLYGON IN LNGLAT
        LngLat b = new LngLat(1, -2);
        System.out.println(b.inCentralArea());
        //expected true

        LngLat c = new LngLat(-1, -4);
        System.out.println(c.inCentralArea());
        // expected false

        LngLat d = new LngLat(1, -4);
        System.out.println(d.inCentralArea());
        //one of the boundary points, so expected true

        // ONLY MAKES SENSE WITH 2ND NEW TESTING POLYGON IN LNGLAT
        LngLat e = new LngLat(-1, -1);
        System.out.println(e.inCentralArea());
        // expected true

        LngLat f = new LngLat(0, -1);
        System.out.println(f.inCentralArea());
        // one of the points, so expected true

        LngLat g = new LngLat(-3, -2);
        System.out.println(g.inCentralArea());
        // expected false
    }
}
