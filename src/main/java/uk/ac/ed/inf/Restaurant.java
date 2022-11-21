package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URL;

/**
 * Represents a participating restaurant in the drone delivery service
 */
// in theory this could also be a record, but the spec mentioned a getMenu method specifically, so I just
// went for the normal class structure, and now I'm afraid to change it in case I break something
public class Restaurant {
    private String name;
    private LngLat location;
    private Menu[] menu;

    /**
     * @param name The name of the restaurant
     * @param longitude The longitude coordinate of the restaurant
     * @param latitude The latitude coordinate of the restaurants
     * @param menu List of menu items offered by the restaurant
     */
    public Restaurant(@JsonProperty("name") String name, @JsonProperty("longitude") double longitude,
                      @JsonProperty("latitude") double latitude, @JsonProperty("menu") Menu[] menu) {
        this.name = name;
        this.location = new LngLat(longitude, latitude);
        this.menu = menu;
    }

    /**
     * @return the menu items for this restaurant
     */
    public Menu[] getMenu() { return menu; }

    /**
     * Retrieves a list of restaurants from the REST server URL provided
     * @param serverBaseAddress URL of the REST server
     * @return Current list of restaurants
     * @throws IOException if the URL is incorrect
     */
    public static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) throws IOException {
        DataRetrieval.setBaseURL(String.valueOf(serverBaseAddress));

        return DataRetrieval.retrieveDataFromURL("restaurants", new TypeReference<>(){});
    }
}
