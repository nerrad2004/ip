package nerrad.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents a task that must be completed by a specific date or time.
 */
public class Deadline extends Task {
    /** Format used to present a deadline date in the chatbot UI. */
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    /** Date by which the task should be completed. */
    protected LocalDate by;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description description of the deadline
     * @param by date by which the task should be completed
     */
    public Deadline(String description, LocalDate by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the due date for saving and other date-based operations.
     *
     * @return deadline date
     */
    public LocalDate getBy() {
        return by;
    }

    /**
     * Returns the deadline in its user-facing display format.
     *
     * @return deadline type icon, common task display, and formatted due date
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by.format(DISPLAY_DATE_FORMAT) + ")";
    }
}

