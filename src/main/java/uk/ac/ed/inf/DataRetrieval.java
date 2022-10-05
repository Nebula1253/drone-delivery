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

    private DataRetrieval() {}

    // ideally I wouldn't be passing a TypeReference object here since that's Jackson-specific
    // and the whole point is that this is the one and only function you'd need to change if, suppose,
    // your method of accessing the data changes... however, it screwed up the type of the object returned
    // if I created a new TypeReference object inside the function (for some strange reason)
    public static <T> T retrieveDataFromURL(String url, TypeReference<T> typeRef) throws IOException {
        return mapper.readValue(new URL(url), typeRef);
    }
}
