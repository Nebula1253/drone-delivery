package uk.ac.ed.inf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

public class OrderValidationTest {
    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println(testInfo.getDisplayName());
    }

    @Test
    void expiryDateValidation() {
        Order validDate = new Order("1234", "2023-01-01", "Per Son", "4658565781486112",
                "04/23", "456", 1100, new ArrayList<>(List.of("Margarita")));
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

        assertEquals(validDate.getOutcome(), OrderOutcome.ValidButNotDelivered);
        assertEquals(dateBeforeNow.getOutcome(), OrderOutcome.InvalidExpiryDate);
        assertEquals(dateBeforeOrderDateButAfterNow.getOutcome(), OrderOutcome.InvalidExpiryDate);
        assertEquals(expiryInTheSameMonthAsOrder.getOutcome(), OrderOutcome.ValidButNotDelivered);
        assertEquals(invalidExpiryDateFormat.getOutcome(), OrderOutcome.InvalidExpiryDate);
        assertEquals(notEvenADate.getOutcome(), OrderOutcome.InvalidExpiryDate);
    }
}
