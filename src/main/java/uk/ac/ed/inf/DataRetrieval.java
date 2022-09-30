package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

//TODO: fix this bullshit to return the correct type
public final class DataRetrieval {
    public static <T> T retrieveDataFromURL(String url) throws IOException {
        return (new ObjectMapper()).readValue(new URL(url), new TypeReference<>(){});
    }
}
