package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

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

    public Menu[] getMenu() { return menu; }

    public static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) throws IOException {
        return new ObjectMapper().readValue(new URL(serverBaseAddress.getProtocol() + "://" +
                serverBaseAddress.getHost() + "/restaurants"), new TypeReference<>(){});
    }
}
