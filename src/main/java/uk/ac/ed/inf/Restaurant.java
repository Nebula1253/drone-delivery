package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URL;

/**
 * Represents a participating restaurant in the drone delivery service
 */
// in theory this could also be a record, but the spec mentioned a getMenu method specifically, so I just
// went for the normal class structure
public class Restaurant {
    @JsonProperty("name")
    private String name;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("menu")
    private Menu[] menu;

    public Restaurant() {
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
