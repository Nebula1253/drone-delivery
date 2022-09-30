package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Arrays;

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
    // constant value representing delivery cost
    private static final int DELIVERY_COST = 100;

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
        int itemsRemaining = itemsOrdered.length;
        Menu[] currentMenu;
        int totalCost = DELIVERY_COST;
        boolean restaurantFound = false;

        for (Restaurant r : restaurants) {
            currentMenu = r.getMenu();

            for (Menu m : currentMenu) {
                //System.out.println(r.name + " " + m.name());
                if (Arrays.asList(itemsOrdered).contains(m.name())) {
                    totalCost += m.priceInPence();
                    itemsRemaining--;
                    restaurantFound = true;
                }
            }

            if (restaurantFound && itemsRemaining != 0) {
                throw new InvalidPizzaCombinationException("You can't order pizzas from 2 restaurants at once!");
            }
        }

        return totalCost;
    }
}
