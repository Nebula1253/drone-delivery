package uk.ac.ed.inf;

/**
 *
 * @param name
 * @param longitude
 * @param latitude
 * @param menu
 */
public record Restaurant(String name, double longitude, double latitude, Menu[] menu) {
}
