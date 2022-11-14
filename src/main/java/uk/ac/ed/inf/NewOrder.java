package uk.ac.ed.inf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewOrder {
    private String orderNo, orderDate, customer, creditCardNumber, creditCardExpiry, cvv;
    private int priceTotalInPence;
    private ArrayList<String> orderItems;
    private OrderOutcome outcome = OrderOutcome.ValidButNotDelivered;

    public NewOrder(String orderNo, String orderDate, String customer, String creditCardNumber, String creditCardExpiry,
                    int priceTotalInPence, ArrayList<String> orderItems) {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.customer = customer;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.priceTotalInPence = priceTotalInPence;
        this.orderItems = orderItems;

        validate();
    }

    private void validate() {
        // Expiry date is invalid if: a) it's in the past, b) it's not in the "mm/yy" pattern
        try {
            LocalDate expiryDate = LocalDate.parse(this.creditCardExpiry, DateTimeFormatter.ofPattern("MM/yy"));
            if (expiryDate.isBefore(LocalDate.now())) {
                this.outcome = OrderOutcome.InvalidExpiryDate;
            }
        }
        catch(Exception e) { this.outcome = OrderOutcome.InvalidExpiryDate; }

        // The CVV is invalid if it's longer than 3-4 characters and it contains non-numeric characters
        Matcher m = Pattern.compile("^[0-9]{3,4}$").matcher(this.cvv);
        if (!m.matches()) { this.outcome = OrderOutcome.InvalidCvv; }


    }
}
