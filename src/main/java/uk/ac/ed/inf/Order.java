package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Arrays;

public record Order(String orderNo, String orderDate, String customer, String creditCardNumber,
                    String creditCardExpiry, String cvv, int priceTotalInPence, ArrayList<String> orderItems) {
    private static final int DELIVERY_COST = 100;

    public int getDeliveryCost(Restaurant[] restaurants, String... itemsOrdered) throws InvalidPizzaCombinationException {
        int itemsRemaining = itemsOrdered.length;
        Menu[] currentMenu;
        int totalCost = DELIVERY_COST;
        boolean restaurantFound = false;

        for (Restaurant r : restaurants) {
            currentMenu = r.getMenu();

            for (Menu m : currentMenu) {
                if (restaurantFound && itemsRemaining != 0) {
                    throw new InvalidPizzaCombinationException("You can't order pizzas from 2 restaurants at once!");
                }

                if (Arrays.asList(itemsOrdered).contains(m.name())) {
                    totalCost += m.priceInPence();
                    itemsRemaining--;
                    restaurantFound = true;
                }
            }
        }

        return totalCost;
    }
}
