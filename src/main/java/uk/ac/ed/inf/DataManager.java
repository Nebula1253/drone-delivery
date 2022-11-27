package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mapbox.geojson.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Utility class used for retrieving data from a provided URL
 */
public final class DataManager {
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static String baseURL = "https://ilp-rest.azurewebsites.net/";
    private static String baseFilePath = "./resultfiles/";

    private DataManager() {}

    /**
     * Utility method for retrieving data from REST servers
     * @param endpointName Name of endpoint to access resources from
     * @param typeRef TypeReference object needed for jackson to know the Java class to read data as
     * @return The data retrieved
     * @param <T> class that data needs to be retrieved as
     */
    // ideally I wouldn't be passing a TypeReference object here since that's Jackson-specific
    // and the whole point is that this is the one and only function you'd need to change if, suppose,
    // your method of accessing the data changes... however, it screwed up the type of the object returned
    // if I created a new TypeReference object inside the function (for some strange reason)
    public static <T> T retrieveDataFromURL(String endpointName, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(new URL(baseURL + endpointName), typeRef);
        }
        catch(IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
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

    /**
     *
     * @param filename
     * @param object
     */
    public static void writeToJSONFile(String filename, Object object) {
        try {
            new File(baseFilePath).mkdir();
            mapper.writeValue(new File(baseFilePath + filename), object);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     *
     * @param filename
     * @param points
     */
    public static void writeToGeoJSONFile(String filename, ArrayList<LngLat> points) {
        ArrayList<Point> geoJsonPoints = new ArrayList<>();
        for (LngLat point : points) {
            geoJsonPoints.add(Point.fromLngLat(point.lng(), point.lat()));
        }

        String jsonString = FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(geoJsonPoints))).toJson();
        try {
            new File(baseFilePath).mkdir();
            BufferedWriter writer = new BufferedWriter(new FileWriter(baseFilePath + filename));
            writer.write(jsonString);
            writer.close();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
