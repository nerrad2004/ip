package nerrad.task;

/**
 * Represents a task that occurs between a start and an end date or time.
 */
public class Event extends Task {
    /** Date or time at which the event starts. */
    protected String from;

    /** Date or time at which the event ends. */
    protected String to;

    /**
     * Creates an incomplete event task.
     *
     * @param description description of the event
     * @param from date or time at which the event starts
     * @param to date or time at which the event ends
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the start date or time as entered by the user.
     *
     * @return event start date or time
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns the end date or time as entered by the user.
     *
     * @return event end date or time
     */
    public String getTo() {
        return to;
    }

    /**
     * Returns the event in its user-facing display format.
     *
     * @return event type icon, common task display, and start/end details
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}

