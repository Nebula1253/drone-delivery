package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Singleton class representing central area coordinates, made immutable once the object has been instantiated
 */
public final class CentralArea {
    private static CentralArea INSTANCE;
    private static String baseURL = "https://ilp-rest.azurewebsites.net/";
    public ArrayList<LngLat> points;

    private CentralArea() throws IOException {
        points = DataRetrieval.retrieveDataFromURL(baseURL + "centralArea", new TypeReference<>(){});
    }

    /**
     * Accesses the singleton instance
     * @return the singleton instance
     * @throws IOException if there's an error in getting the central area coordinates from the server
     */
    public static CentralArea getInstance() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new CentralArea();
        }
        return INSTANCE;
    }

    public ArrayList<LngLat> getCentralArea(){
        return points;
    }

    // presumably the mechanism by which the URL can be changed in CW2, but this will probably have to be changed
    // only really included it because the spec mentioned it

    /**
     * Changes the base server URL to a new value, if required
     * @param newURL the URL to be changed to
     */
    public static void setURL(String newURL) {
        baseURL = newURL;
    }
}
