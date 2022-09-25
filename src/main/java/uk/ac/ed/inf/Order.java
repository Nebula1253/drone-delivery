package uk.ac.ed.inf;

public record Order(String orderNo, String orderDate, String customer, String creditCardNumber,
                    String creditCardExpiry, String cvv, int priceTotalInPence, String[] orderItems) {
    //@TODO: getDeliveryCost
}
