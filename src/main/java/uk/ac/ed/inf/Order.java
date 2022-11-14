package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Arrays;

// Strictly speaking these don't come into use until CW2, but I thought I'd include them here anyway
/**
 * Represents the information of an order placed with the drone delivery service
 * @param orderNo Unique number assigned to the order
 * @param orderDate Date the order was placed
 * @param customer Name of customer who placed the order
 * @param creditCardNumber Credit card number used for payment
 * @param creditCardExpiry Credit card expiry date used for payment
 * @param cvv Credit card CVV used for payment
 * @param priceTotalInPence Total price of the menu items, given in pence
 * @param orderItems Names of menu items ordered
 */
public record Order(String orderNo, String orderDate, String customer, String creditCardNumber,
                    String creditCardExpiry, String cvv, int priceTotalInPence, ArrayList<String> orderItems) {
    // constant value representing delivery fee
    private static final int DELIVERY_FEE = 100;
    // TODO: this can't be static, maybe convert order to a class?
    private static OrderOutcome outcome;

    /**
     * Determines the cost of having order items delivered by drone, including the 1-pound delivery cost, and checks whether
     * the combination of order items is valid
     * @param restaurants An array of the participating restaurants
     * @param itemsOrdered A variable number of strings to represent the items ordered
     * @return The delivery cost as an integer
     * @throws InvalidPizzaCombinationException if the items ordered don't all come from the same restaurant,
     * or if some items don't actually exist
     */
    public int getDeliveryCost(Restaurant[] restaurants, String... itemsOrdered) throws InvalidPizzaCombinationException {
        // you could just add the delivery fee to the priceTotalInPence field, but that could be inconsistent with the actual listed costs,
        // and since you're iterating through the menu items anyway, may as well add it up that way
        int itemsRemaining = itemsOrdered.length;
        Menu[] currentMenu;
        int totalCost = DELIVERY_FEE;
        boolean restaurantFound = false;

        for (Restaurant r : restaurants) {
            currentMenu = r.getMenu();

            // iterate through every single menu item in the current restaurant
            for (Menu m : currentMenu) {
                // if this item has been ordered, add cost and flag that the restaurant has been found
                if (Arrays.asList(itemsOrdered).contains(m.name())) {
                    totalCost += m.priceInPence();
                    itemsRemaining--;
                    restaurantFound = true;
                }
            }

            // if you've finished iterating through all the items for this restaurant, and there are still
            // items unaccounted for on the order, clearly something is wrong
            if (restaurantFound && itemsRemaining != 0) {
                throw new InvalidPizzaCombinationException("");
            }
        }

        return totalCost;
    }
}
