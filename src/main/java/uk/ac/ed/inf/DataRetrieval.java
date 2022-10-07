package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

/**
 * Utility class used for retrieving data from a provided URL
 */
public final class DataRetrieval {
    static final ObjectMapper mapper = new ObjectMapper();
    private static String baseURL = "https://ilp-rest.azurewebsites.net/";

    private DataRetrieval() {}

    /**
     * Utility method for retrieving data from REST servers
     * @param endpointName Name of endpoint to access resources from
     * @param typeRef TypeReference object needed for jackson to know the Java class to read data as
     * @return The data retrieved
     * @param <T> class that data needs to be retrieved as
     * @throws IOException if data retrieval fails (URL of incorrect format, endpoint doesn't exist, etc.)
     */
    // ideally I wouldn't be passing a TypeReference object here since that's Jackson-specific
    // and the whole point is that this is the one and only function you'd need to change if, suppose,
    // your method of accessing the data changes... however, it screwed up the type of the object returned
    // if I created a new TypeReference object inside the function (for some strange reason)
    public static <T> T retrieveDataFromURL(String endpointName, TypeReference<T> typeRef) throws IOException {
        return mapper.readValue(new URL(baseURL + endpointName), typeRef);
    }

    /**
     * Allows changing the server base URL from the default value
     * @param newURL URL to be changed to
     */
    public static void setBaseURL(String newURL) {
        // ensures that the endpoint names are appended properly when retrieving values
        if ((newURL.charAt(newURL.length() - 1)) != '/') { newURL += "/"; }
        baseURL = newURL;
    }
}
