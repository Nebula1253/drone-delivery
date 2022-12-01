package uk.ac.ed.inf;

/**
 * Represents a menu item in a participating restaurant
 * @param name Name of the item
 * @param priceInPence Price of the item, given in pence
 */
public record MenuItem(String name, int priceInPence) {
}
