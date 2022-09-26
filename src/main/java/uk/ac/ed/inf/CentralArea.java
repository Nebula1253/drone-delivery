package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public final class CentralArea {
    private static CentralArea INSTANCE;
    private static final String BASE_URL = "https://ilp-rest.azurewebsites.net/";
    private static List<LngLat> points = null;

    private CentralArea() throws IOException {
        points = new ObjectMapper().readValue(new URL(BASE_URL + "centralArea"), new TypeReference<>(){});
    }

    public static CentralArea getInstance() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new CentralArea();
        }
        return INSTANCE;
    }

    public List<LngLat> getCentralArea(){
        return points;
    }
}
