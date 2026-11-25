package scheduler.util;

import java.util.ArrayList;
import java.util.List;

/** Input parsing with clear messages. Never throws anything but {@link ValidationException}. */
public final class Validation {
    public static final int MAX_PROCESSES = 50;
    public static final int MAX_TIME_VALUE = 9999;
    public static final int MAX_PRIORITY = 999;
    public static final int MAX_REQUESTS = 500;
    public static final int MAX_DISK_SIZE = 100000;
    public static final int DEFAULT_DISK_SIZE = 200;

    private Validation() { }

    /** Parses a whole number in [min, max]; {@code label} names the field in messages. */
    public static int parseInt(String label, String text, int min, int max) throws ValidationException {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) throw new ValidationException(label + " is required (it cannot be empty).");
        if (!t.matches("[+-]?\\d+"))
            throw new ValidationException(label + " must be a whole number (got \"" + shorten(t) + "\").");
        long value;
        try {
            value = Long.parseLong(t.startsWith("+") ? t.substring(1) : t);
        } catch (NumberFormatException e) {
            throw new ValidationException(label + " is out of range (allowed " + min + " to " + max + ").");
        }
        if (value < min || value > max)
            throw new ValidationException(label + " must be between " + min + " and " + max + " (got " + value + ").");
        return (int) value;
    }

    /** Parses a list of cylinder numbers separated by commas, spaces or semicolons. */
    public static List<Integer> parseRequests(String text, int diskSize) throws ValidationException {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) throw new ValidationException("The request queue is empty. Enter cylinder numbers, e.g. 98, 183, 37.");
        String[] tokens = t.split("[,;\\s]+");
        if (tokens.length > MAX_REQUESTS)
            throw new ValidationException("Too many requests (maximum " + MAX_REQUESTS + ").");
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            out.add(parseInt("Request #" + (i + 1), tokens[i], 0, diskSize - 1));
        }
        return out;
    }

    private static String shorten(String s) {
        return s.length() > 20 ? s.substring(0, 20) + "..." : s;
    }
}
