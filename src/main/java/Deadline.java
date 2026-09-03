/**
 * Represents a task that must be completed by a specific date or time.
 */
public class Deadline extends Task {
    /** Date or time by which the task should be completed. */
    protected String by;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description description of the deadline
     * @param by date or time by which the task should be completed
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the due date or time as entered by the user.
     *
     * @return deadline date or time
     */
    public String getBy() {
        return by;
    }

    /**
     * Returns the deadline in its user-facing display format.
     *
     * @return deadline type icon, common task display, and due date/time
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
