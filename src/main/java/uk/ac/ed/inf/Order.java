package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

public class Order {
    private String orderNo, orderDate, customer, creditCardNumber, creditCardExpiry, cvv;
    private int priceTotalInPence;
    private ArrayList<String> orderItems;
    private OrderOutcome outcome = OrderOutcome.ValidButNotDelivered;
    private LngLat deliveryLocation;

    // constant value representing delivery fee
    private static final int DELIVERY_FEE = 100;

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
    public Order(String orderNo, String orderDate, String customer, String creditCardNumber, String creditCardExpiry,
                 String cvv, int priceTotalInPence, ArrayList<String> orderItems) throws IOException {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.customer = customer;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.cvv = cvv;
        this.priceTotalInPence = priceTotalInPence;
        this.orderItems = orderItems;

        validate();
    }

    private void validate() throws IOException {
        validateExpiryDate();
        validateCVV();
        validateOrderItems();
        validateCreditCardNumber();
    }

    private void validateExpiryDate() {
        // Expiry date is invalid if: a) it's in the past, b) it's not in the "mm/yy" pattern
        try {
            // the expiry date always refers to the very last day of the month, so the object is adjusted accordingly
            LocalDate expiryDate = LocalDate.parse(this.creditCardExpiry, DateTimeFormatter.ofPattern("MM/yy"))
                    .with(TemporalAdjusters.lastDayOfMonth());
            if (expiryDate.isBefore(LocalDate.now())) {
                this.outcome = OrderOutcome.InvalidExpiryDate;
            }
        }
        catch(Exception e) { this.outcome = OrderOutcome.InvalidExpiryDate; }
    }

    private void validateCVV() {
        // The CVV is invalid if it's longer than 3-4 characters and it contains non-numeric characters
        if (!this.cvv.matches("^[0-9]{3,4}$")) { this.outcome = OrderOutcome.InvalidCvv; }
    }

    //TODO: there is probably a better way to do this
    private void validateOrderItems() throws IOException {
        int itemsRemaining = this.orderItems.size();
        if (itemsRemaining > 4) { this.outcome = OrderOutcome.InvalidPizzaCount; }
        Menu[] currentMenu;
        int totalCost = 0;
        boolean restaurantFound = false;

        // maybe this should be changed? if you're intending to have a reference to a restaurant stored within the order,
        // then maybe you shouldn't read into a local array? if you have the restaurant stored elsewhere then they wouldn't be the
        // same object internally
        Restaurant[] allRestaurants = DataRetrieval.retrieveDataFromURL("restaurants", new TypeReference<>(){});
        for (Restaurant r : allRestaurants) {
            currentMenu = r.getMenu();

            // iterate through every single menu item in the current restaurant
            for (Menu m : currentMenu) {
                // if this item has been ordered, add cost and flag that the restaurant has been found
                if (this.orderItems.contains(m.name())) {
                    totalCost += m.priceInPence();
                    itemsRemaining--;
                    restaurantFound = true;
                    // set restaurant reference????
                }
            }

            if (restaurantFound) {
                // if you've finished iterating through all the items for this restaurant, found a match for one or more items in the order,
                // and there are still items unaccounted for on the order, clearly something is wrong
                if (itemsRemaining != 0) {
                    this.outcome = OrderOutcome.InvalidPizzaCombinationMultipleSuppliers;
                }
                // if the restaurant was found and all the menu items were accounted for, set the delivery location
                else {
                    this.deliveryLocation = new LngLat(r.getLongitude(), r.getLatitude());
                }
            }
        }
        // if you've gone through ALL the menu items of all the participating restaurants, and there are still remaining items,
        // those items are then invalid
        if (itemsRemaining != 0) { this.outcome = OrderOutcome.InvalidPizzaNotDefined; }

        // we've manually totalled up the price for all the items, including the delivery fee, so if this is inconsistent
        // with the provided order total, we need to set the outcome accordingly
        if (totalCost != this.priceTotalInPence) { this.outcome = OrderOutcome.InvalidTotal; }
    }

    private void validateCreditCardNumber(){
        String[] creditCardDigits = this.creditCardNumber.split("");

        if (this.creditCardNumber.matches("[0-9]+")) { // contains no non-numeric characters, so that the parseint never fails
            int firstTwoDigits = Integer.parseInt(creditCardDigits[0] + creditCardDigits[1]);
            if (this.creditCardNumber.length() == 16 && // Standard credit card number length
                    (creditCardDigits[0].equals("4") || // Visa prefix
                            (firstTwoDigits >= 51 && firstTwoDigits <= 55))) // Mastercard prefix
            {
                // credit card validation: Luhn's algorithm
                int sumOfDoubledDigits = 0, sumOfRemainingDigits = 0;
                for (int i = creditCardDigits.length - 1; i >= 0; i--) {
                    if (i % 2 == 0) {
                        String[] doubledDigit = (Integer.toString(Integer.parseInt(creditCardDigits[i]) * 2)).split("");
                        for (String s: doubledDigit) {
                            sumOfDoubledDigits += Integer.parseInt(s);
                        }
                    }
                    else {
                        sumOfRemainingDigits += Integer.parseInt(creditCardDigits[i]);
                    }
                }
                if ((sumOfDoubledDigits+sumOfRemainingDigits) % 10 != 0) {
                    this.outcome = OrderOutcome.InvalidCardNumber;
                }
            }
            else { this.outcome = OrderOutcome.InvalidCardNumber; }
        }
        else { this.outcome = OrderOutcome.InvalidCardNumber; }

    }

    /**
     * Determines the cost of having order items delivered by drone, including the 1-pound delivery cost
     */
    public int getDeliveryCost() {
        if (this.outcome != OrderOutcome.InvalidTotal) {
            return this.priceTotalInPence + DELIVERY_FEE;
        }
        return -1;
    }

    public LngLat getDeliveryLocation() {
        return deliveryLocation;
    }
}
