package uk.ac.ed.inf;

import java.net.URL;

public record Restaurant(String name, double longitude, double latitude, Menu[] menu) {
    public static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) {
        //@TODO: implement
        return null;
    }
}
