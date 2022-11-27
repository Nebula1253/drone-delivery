package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

public class Order {
    private String orderNo, orderDate, customer, creditCardNumber, creditCardExpiry, cvv;
    private int priceTotalInPence;

    private ArrayList<String> orderItems;

    private OrderOutcome outcome = OrderOutcome.ValidButNotDelivered;

    // this is only set to the restaurant's location once the orderItems validation is done, so I need a default value
    @JsonIgnore
    private LngLat deliveryLocation = new LngLat(19.0760, 72.8777);

    // constant value representing delivery fee
    @JsonIgnore
    private static final int DELIVERY_FEE = 100;

    private static final Restaurant[] allRestaurants = DataManager.retrieveDataFromURL("restaurants", new TypeReference<>(){});

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

    private void validate() {
        validateExpiryDate();
        validateCVV();
        validateCreditCardNumber();
        validateOrderItems();

        if (outcome != OrderOutcome.ValidButNotDelivered) System.out.println(outcome);
    }

    private void validateExpiryDate() {
        // Expiry date is invalid if: a) it's in the past, b) it's not in the "mm/yy" pattern
        try {
            // the expiry date always refers to the very last day of the month, so the object is adjusted accordingly
            LocalDate expiryDate = LocalDate.parse("01/" + this.creditCardExpiry, DateTimeFormatter.ofPattern("dd/MM/yy"))
                    .with(TemporalAdjusters.lastDayOfMonth());
            LocalDate orderDate = LocalDate.parse(this.orderDate, DateTimeFormatter.ISO_LOCAL_DATE);

            if (expiryDate.isBefore(orderDate)) {
                this.outcome = OrderOutcome.InvalidExpiryDate;
            }
        }
        catch(Exception e) { this.outcome = OrderOutcome.InvalidExpiryDate; }
    }

    private void validateCVV() {
        if (outcome == OrderOutcome.ValidButNotDelivered) {
            // The CVV is invalid if it's longer than 3 characters or if it contains non-numeric characters
            if (!this.cvv.matches("^[0-9]{3}$")) { this.outcome = OrderOutcome.InvalidCvv; }
        }
    }

    private void validateOrderItems() {
        if (outcome == OrderOutcome.ValidButNotDelivered) {
            LngLat restaurantLocation = new LngLat(0,0);
            int itemsRemaining = this.orderItems.size();
            if (itemsRemaining > 4) { this.outcome = OrderOutcome.InvalidPizzaCount; return; }
            Menu[] currentMenu;
            int totalCost = DELIVERY_FEE;
            boolean restaurantFound = false;

            for (Restaurant r : allRestaurants) {
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
                    this.outcome = OrderOutcome.InvalidPizzaCombinationMultipleSuppliers;
                    //System.out.println(orderItems);
                    //break;
                }
            }
            // if you've gone through ALL the menu items of all the participating restaurants, and there are still remaining items,
            // those items are then invalid
            if (itemsRemaining > 0) {
                //System.out.println(orderItems);
                this.outcome = OrderOutcome.InvalidPizzaNotDefined;
                return;
            }

            // we've manually totalled up the price for all the items, including the delivery fee, so if this is inconsistent
            // with the provided order total, we need to set the outcome accordingly
            if (totalCost != this.priceTotalInPence) {
                //System.out.println(totalCost + " " + priceTotalInPence);
                this.outcome = OrderOutcome.InvalidTotal;
                return;
            }

            if (this.outcome == OrderOutcome.ValidButNotDelivered) this.deliveryLocation = restaurantLocation;
        }
    }

    private void validateCreditCardNumber(){
        if (outcome == OrderOutcome.ValidButNotDelivered) {
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
                    this.outcome = OrderOutcome.InvalidCardNumber;
                }
            }
            else {
                this.outcome = OrderOutcome.InvalidCardNumber;
                //System.out.println(creditCardNumber);
            }
        }
    }

    /**
     * Determines the cost of having order items delivered by drone, including the 1-pound delivery cost
     */
    public int getCostInPence() {
        return this.priceTotalInPence + DELIVERY_FEE;
    }

    public OrderOutcome getOutcome() {
        return outcome;
    }

    public String getCreditCardExpiry() {
        return creditCardExpiry;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCvv() {
        return cvv;
    }

    public LngLat getDeliveryLocation() {
        return deliveryLocation;
    }

    public void deliver() {
        outcome = OrderOutcome.Delivered;
    }
}
