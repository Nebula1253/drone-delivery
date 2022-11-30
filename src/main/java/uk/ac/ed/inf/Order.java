package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

// this could be a record, but I needed private attributes that weren't part of the constructor (like the outcome and the delivery location)
// and records don't allow for that
public class Order {
    private final String orderNo, orderDate, customer, creditCardNumber, creditCardExpiry, cvv;
    private final int priceTotalInPence;

    private final ArrayList<String> orderItems;

    private OrderOutcome outcome = OrderOutcome.VALID_BUT_NOT_DELIVERED;

    // this is only set to the restaurant's location once the orderItems validation is done, so I need a default value
    @JsonIgnore
    private LngLat pickupLocation = new LngLat(0, 0);

    // constant value representing delivery fee
    @JsonIgnore
    private static final int DELIVERY_FEE = 100;

    @JsonIgnore
    private static boolean testing = false;

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
    public Order(@JsonProperty("orderNo") String orderNo,  @JsonProperty("orderDate") String orderDate,
                 @JsonProperty("customer") String customer,  @JsonProperty("creditCardNumber") String creditCardNumber,
                 @JsonProperty("creditCardExpiry") String creditCardExpiry,  @JsonProperty("cvv") String cvv,
                 @JsonProperty("priceTotalInPence") int priceTotalInPence,  @JsonProperty("orderItems") ArrayList<String> orderItems) {
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

    // Checks all the order details
    private void validate() {
        validateCVV();
        validateExpiryDate();
        validateCreditCardNumber();
        validateOrderItems();
    }

    // Checks if the CVV provided for the payment card is valid; if it isn't, changes the order outcome accordingly
    private void validateCVV() {
        // The CVV is invalid if it's longer than 3 characters or if it contains non-numeric characters
        if (!this.cvv.matches("^[0-9]{3}$")) { this.outcome = OrderOutcome.INVALID_CVV; }
    }

    // Checks if the expiry date of the payment card provided is before the order date; if the card has expired, changes the order outcome accordingly
    private void validateExpiryDate() {
        if (this.outcome == OrderOutcome.VALID_BUT_NOT_DELIVERED) {
            // Expiry date is invalid if: a) it's in the past, b) it's not in the "mm/yy" pattern
            try {
                // the expiry date always refers to the very last day of the month, so the object is adjusted accordingly
                LocalDate expiryDate = LocalDate.parse("01/" + this.creditCardExpiry, DateTimeFormatter.ofPattern("dd/MM/yy"))
                        .with(TemporalAdjusters.lastDayOfMonth());
                LocalDate orderDate = LocalDate.parse(this.orderDate, DateTimeFormatter.ISO_LOCAL_DATE);

                if (expiryDate.isBefore(orderDate)) {
                    this.outcome = OrderOutcome.INVALID_EXPIRY_DATE;
                }
            }
            catch(Exception e) { this.outcome = OrderOutcome.INVALID_EXPIRY_DATE; }
        }
    }

    // Checks if the payment card number is valid (i.e. is the correct length, is either a Visa or a MasterCard, and passes the Luhn's algorithm check)
    private void validateCreditCardNumber(){
        if (outcome == OrderOutcome.VALID_BUT_NOT_DELIVERED) {
            String[] creditCardDigits = this.creditCardNumber.split("");
            if (this.creditCardNumber.length() == 16 && // Standard credit card number length
                    (this.creditCardNumber.matches("^4[0-9]*$") || // Visa prefix
                    (this.creditCardNumber.matches("^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[01]|2720)[0-9]*$")))) // Mastercard prefix
            {
                // credit card validation: Luhn's algorithm
                int sumOfDigits = 0;
                for (int i = creditCardDigits.length - 1; i >= 0; i--) {
                    if ((creditCardDigits.length - i) % 2 == 0) {
                        int doubledDigit = Integer.parseInt(creditCardDigits[i]) * 2;
                        if (doubledDigit > 9) doubledDigit -= 9;
                        sumOfDigits += doubledDigit;
                    }
                    else sumOfDigits += Integer.parseInt(creditCardDigits[i]);
                }
                if (sumOfDigits % 10 != 0) {
                    this.outcome = OrderOutcome.INVALID_CARD_NUMBER;
                }
            }
            else {
                this.outcome = OrderOutcome.INVALID_CARD_NUMBER;
                //System.out.println(creditCardNumber);
            }
        }
    }

    /*
     * Checks if the items ordered are valid (i.e. all valid item names, all from the same restaurant), as well as if the total price provided is actually correct
     * If these checks fail, sets the order outcome accordingky
     * If these checks <b>succeed</b>, sets the pickup location of the order to the restaurant's location
     */
    private void validateOrderItems() {
        if (outcome == OrderOutcome.VALID_BUT_NOT_DELIVERED) {
            LngLat restaurantLocation = new LngLat(0,0);
            int itemsRemaining = this.orderItems.size();
            if (itemsRemaining > 4) { this.outcome = OrderOutcome.INVALID_PIZZA_COUNT; return; }
            Menu[] currentMenu;
            int totalCost = DELIVERY_FEE;
            boolean restaurantFound = false;

            // here for testing only, so that running OrderValidationTest is not dependent on the variable App.restaurants
            Restaurant[] restaurants;
            if (testing) restaurants = DataManager.retrieveDataFromURL("restaurants", new TypeReference<>(){});
            else restaurants = App.restaurants;

            for (Restaurant r : restaurants) {
                currentMenu = r.menu();

                // iterate through every single menu item in the current restaurant
                for (Menu m : currentMenu) {
                    // if this item has been ordered, add cost and flag that the restaurant has been found
                    if (this.orderItems.contains(m.name())) {
                        totalCost += m.priceInPence();
                        itemsRemaining--;
                        restaurantLocation = new LngLat(r.longitude(), r.latitude());
                        restaurantFound = true;
                    }
                }

                if (restaurantFound && itemsRemaining > 0) {
                    this.outcome = OrderOutcome.INVALID_PIZZA_COMBINATION_MULTIPLE_SUPPLIERS;
                    //System.out.println(orderItems);
                    //break;
                }
            }
            // if you've gone through ALL the menu items of all the participating restaurants, and there are still remaining items,
            // those items are then invalid
            if (itemsRemaining > 0) {
                //System.out.println(orderItems);
                this.outcome = OrderOutcome.INVALID_PIZZA_NOT_DEFINED;
                return;
            }

            // we've manually totalled up the price for all the items, including the delivery fee, so if this is inconsistent
            // with the provided order total, we need to set the outcome accordingly
            if (totalCost != this.priceTotalInPence) {
                //System.out.println(totalCost + " " + priceTotalInPence);
                this.outcome = OrderOutcome.INVALID_TOTAL;
                return;
            }

            if (this.outcome == OrderOutcome.VALID_BUT_NOT_DELIVERED) this.pickupLocation = restaurantLocation;
        }
    }

    /**
     * @return The location for the drone get the order items from
     */
    @JsonIgnore // because we don't want this written into the deliveries file
    public LngLat getPickupLocation() { return pickupLocation; }

    // getters used for JSON serialisation

    /**
     * @return The 8-character unique identifier for the order
     */
    public String getOrderNo() { return orderNo; }

    /**
     * @return The outcome of the order (delivered, valid but not delivered, invalid)
     */
    public OrderOutcome getOutcome() { return outcome; }

    /**
     * @return The total cost of the order, including the delivery fee
     */
    public int getCostInPence() { return this.priceTotalInPence + DELIVERY_FEE; }

    /**
     * Sets the order outcome correctly once the drone has delivered the order to Appleton
     */
    public void deliver() {
        outcome = OrderOutcome.DELIVERED;
    }

    // SPECIFICALLY HERE FOR TESTING PURPOSES: allows order items check to get restaurant data directly from server rather than from App.restaurants,
    // which would have caused an error otherwise if the order was created in a JUnit test class
    public static void setTesting() { testing = true; }
}
