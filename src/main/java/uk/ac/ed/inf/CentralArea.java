package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

//TODO: maybe look into structure here
public final class CentralArea {
    private static CentralArea INSTANCE;
    private static final String BASE_URL = "https://ilp-rest.azurewebsites.net/";
    private static ArrayList<LngLat> points = null;

    private CentralArea() throws IOException {
        points = DataRetrieval.retrieveDataFromURL(BASE_URL + "centralArea", new TypeReference<>(){});
    }

    public static CentralArea getInstance() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new CentralArea();
        }
        return INSTANCE;
    }

    public static ArrayList<LngLat> getCentralArea(){
        return points;
    }
}
