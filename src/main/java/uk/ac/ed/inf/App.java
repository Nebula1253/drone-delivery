package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        LngLat forSureInCentralArea = new LngLat(-3.19, 55.943);
        System.out.println(forSureInCentralArea.inCentralArea());

        // lmao
        LngLat noFuckingWayItsInThere = new LngLat(420, 69);
        System.out.println(noFuckingWayItsInThere.inCentralArea());

        System.out.println(noFuckingWayItsInThere.distanceTo(forSureInCentralArea));

        System.out.println(forSureInCentralArea.closeTo(new LngLat(-3.190005, 55.9430005)));

        System.out.println(forSureInCentralArea.nextPosition(CompassDirection.NORTH));

        var x = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net/"));

        System.out.println(Arrays.toString(x));

        for (Restaurant r : x) {
            System.out.println(Arrays.toString(r.getMenu()));
        }
    }
}
