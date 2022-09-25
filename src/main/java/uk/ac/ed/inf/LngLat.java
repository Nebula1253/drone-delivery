package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@JsonIgnoreProperties("name")
public record LngLat(
        @JsonProperty("longitude")
        double lng,
        @JsonProperty("latitude")
        double lat) {
    private static final double DIST_TOLERANCE = 0.00015;

    public boolean inCentralArea() throws IOException {
        List<LngLat> test = new ObjectMapper().readValue(new URL("https://ilp-rest.azurewebsites.net/centralArea"), new TypeReference<>(){});

        // @TODO: now that you have the coordinates check if the point is inside the polygon
        return false;
    }
    public double distanceTo(LngLat other) {
        return Math.sqrt(((lng - other.lng) * (lng - other.lng)) +
                ((lat - other.lat) * (lat - other.lat)));
    }

    public boolean closeTo(LngLat other) {
        return (this.distanceTo(other) < DIST_TOLERANCE);
    }

    public LngLat nextPosition(CompassDirection dir) {
        if (dir == null) { return this; }

        double angle = dir.ordinal() * 22.5 * (Math.PI/180);
        double yChange = Math.sin(angle) * DIST_TOLERANCE;
        double xChange = Math.cos(angle) * DIST_TOLERANCE;
        return new LngLat(this.lng + xChange, this.lat + yChange);
    }
}
