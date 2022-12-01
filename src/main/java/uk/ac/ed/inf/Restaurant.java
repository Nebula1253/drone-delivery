package uk.ac.ed.inf;

/**
 * Represents a participating restaurant in the drone delivery scheme
 * @param name The restaurant's name
 * @param longitude The longitude coordinate of the restaurant's location
 * @param latitude The latitude coordinate of the restaurant's location
 * @param menu A list of menu items the restaurant offers
 */
public record Restaurant(String name, double longitude, double latitude, MenuItem[] menu) {
}
