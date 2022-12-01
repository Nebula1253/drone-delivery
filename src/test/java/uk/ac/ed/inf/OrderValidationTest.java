package uk.ac.ed.inf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;


public class OrderValidationTest {
    private final Order validOrderControl;

    OrderValidationTest(){
        Order.setTesting();
        this.validOrderControl = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "04/23", "456", 1100, new ArrayList<>(List.of("Margarita")));
    }

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        //App.restaurants = DataManager.retrieveDataFromURL("restaurants", new TypeReference<Restaurant[]>(){});
        System.out.println(testInfo.getDisplayName());
    }

    @Test
    void expiryDateValidation() {
        Order dateBeforeNow = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "10/22", "456", 1100, new ArrayList<>(List.of("Margarita")));
        Order dateBeforeOrderDateButAfterNow = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "12/22", "456", 1100, new ArrayList<>(List.of("Margarita")));
        Order expiryInTheSameMonthAsOrder = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "01/23", "456", 1100, new ArrayList<>(List.of("Margarita")));
        Order invalidExpiryDateFormat = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "2023-01", "456", 1100, new ArrayList<>(List.of("Margarita")));
        Order notEvenADate = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "woow0eoweoerwr", "456", 1100, new ArrayList<>(List.of("Margarita")));

        assertEquals(validOrderControl.getOutcome(), OrderOutcome.VALID_BUT_NOT_DELIVERED);
        assertEquals(dateBeforeNow.getOutcome(), OrderOutcome.INVALID_EXPIRY_DATE);
        assertEquals(dateBeforeOrderDateButAfterNow.getOutcome(), OrderOutcome.INVALID_EXPIRY_DATE);
        assertEquals(expiryInTheSameMonthAsOrder.getOutcome(), OrderOutcome.VALID_BUT_NOT_DELIVERED);
        assertEquals(invalidExpiryDateFormat.getOutcome(), OrderOutcome.INVALID_EXPIRY_DATE);
        assertEquals(notEvenADate.getOutcome(), OrderOutcome.INVALID_EXPIRY_DATE);
    }

    @Test
    void cvvValidation() {
        Order cvvWrongLength = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "04/23", "4567", 1100, new ArrayList<>(List.of("Margarita")));
        Order cvvNotANumber = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "04/23", "456a", 1100, new ArrayList<>(List.of("Margarita")));

        assertEquals(validOrderControl.getOutcome(), OrderOutcome.VALID_BUT_NOT_DELIVERED);
        assertEquals(cvvWrongLength.getOutcome(), OrderOutcome.INVALID_CVV);
        assertEquals(cvvNotANumber.getOutcome(), OrderOutcome.INVALID_CVV);
    }

    @Test
    void creditCardNumberValidation() {
        Order cardNumberTooLong = new Order("1234", "2023-01-01", "Per Son", "12345678910111213141516",
                "04/23", "456", 1100, new ArrayList<>(List.of("Margarita")));
        Order cardNumberTooShort = new Order("1234", "2023-01-01", "Per Son", "0",
                "04/23", "456", 1100, new ArrayList<>(List.of("Margarita")));
        Order notAVisaOrMastercard = new Order("1234", "2023-01-01", "Per Son", "374392354175134",
                "04/23", "456", 1100, new ArrayList<>(List.of("Margarita")));
        Order doesNotPassLuhnAlgorithm = new Order("1234", "2023-01-01", "Per Son", "4658565781486113",
                "04/23", "456", 1100, new ArrayList<>(List.of("Margarita")));

        assertEquals(validOrderControl.getOutcome(), OrderOutcome.VALID_BUT_NOT_DELIVERED);
        assertEquals(cardNumberTooShort.getOutcome(), OrderOutcome.INVALID_CARD_NUMBER);
        assertEquals(cardNumberTooLong.getOutcome(), OrderOutcome.INVALID_CARD_NUMBER);
        assertEquals(notAVisaOrMastercard.getOutcome(), OrderOutcome.INVALID_CARD_NUMBER);
        assertEquals(doesNotPassLuhnAlgorithm.getOutcome(), OrderOutcome.INVALID_CARD_NUMBER);
    }

    @Test
    void orderItemsValidation() {
        Order wrongTotal = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "04/23", "456", 900, new ArrayList<>(List.of("Margarita")));
        Order wrongPizzaCount = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "04/23", "456", 1100, new ArrayList<>(List.of("Margarita", "Calzone", "Meat Lover", "Vegan Delight", "Super Cheese")));
        Order invalidPizzaCombination = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "04/23", "456", 5000, new ArrayList<>(List.of("Margarita", "Calzone", "Meat Lover", "Vegan Delight")));
        Order wrongPizzas = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "04/23", "456", 1100, new ArrayList<>(List.of("Margarita", "Calzone", "Meat Lover", "McFlurry")));

        assertEquals(validOrderControl.getOutcome(), OrderOutcome.VALID_BUT_NOT_DELIVERED);
        assertEquals(validOrderControl.getPickupLocation(), new LngLat(-3.1912869215011597,55.945535152517735));

        assertEquals(wrongTotal.getOutcome(), OrderOutcome.INVALID_TOTAL);
        assertEquals(wrongTotal.getPickupLocation(), new LngLat(0,0));

        assertEquals(wrongPizzaCount.getOutcome(), OrderOutcome.INVALID_PIZZA_COUNT);
        assertEquals(wrongPizzaCount.getPickupLocation(), new LngLat(0,0));

        assertEquals(wrongPizzas.getOutcome(), OrderOutcome.INVALID_PIZZA_NOT_DEFINED);
        assertEquals(wrongPizzas.getPickupLocation(), new LngLat(0,0));

        assertEquals(invalidPizzaCombination.getOutcome(), OrderOutcome.INVALID_PIZZA_COMBINATION_MULTIPLE_SUPPLIERS);
        assertEquals(invalidPizzaCombination.getPickupLocation(), new LngLat(0,0));
    }
}
