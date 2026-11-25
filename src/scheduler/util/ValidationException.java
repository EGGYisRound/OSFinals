package scheduler.util;

/** Thrown when user input is invalid; the message is shown to the user. */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
