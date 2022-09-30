package uk.ac.ed.inf;

/**
 * Exception thrown when checking delivery cost of order, if items from multiple restaurants are ordered at once or if an item doesn't exist
 */
public class InvalidPizzaCombinationException extends Exception {
    public InvalidPizzaCombinationException(String message) {
        super(message);
    }
}
