package nerrad.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import nerrad.NerradException;
import nerrad.task.Deadline;
import nerrad.task.Event;
import nerrad.task.Task;
import nerrad.task.Todo;

/**
 * Interprets user commands and validates the details contained in them.
 */
public class Parser {
    /**
     * Creates the task subtype specified by a valid user command.
     * Deadlines use a {@link LocalDate} so that they can be validated and
     * displayed consistently.
     *
     * @param input Command entered by the user.
     * @return A todo, deadline, or event represented as a {@link Task}.
     * @throws NerradException If the command does not describe a valid task.
     */
    public Task parseTask(String input) throws NerradException {
        if (input.equals("todo") || input.startsWith("todo ")) {
            String description = input.substring(4).trim();
            if (description.isEmpty()) {
                throw new NerradException("The description of a todo cannot be empty.");
            }
            return new Todo(description);
        }

        if (input.equals("deadline") || input.startsWith("deadline ")) {
            String taskDetails = input.substring(8).trim();
            int byIndex = taskDetails.indexOf("/by");
            if (byIndex == -1) {
                throw new NerradException("A deadline needs a /by date or time.");
            }
            String description = taskDetails.substring(0, byIndex).trim();
            String byText = taskDetails.substring(byIndex + 3).trim();
            if (description.isEmpty()) {
                throw new NerradException("The description of a deadline cannot be empty.");
            }
            if (byText.isEmpty()) {
                throw new NerradException("The /by date or time of a deadline cannot be empty.");
            }
            try {
                return new Deadline(description, LocalDate.parse(byText));
            } catch (DateTimeParseException exception) {
                throw new NerradException("Please use the date format yyyy-MM-dd.");
            }
        }

        if (input.equals("event") || input.startsWith("event ")) {
            String taskDetails = input.substring(5).trim();
            int fromIndex = taskDetails.indexOf("/from");
            int toIndex = taskDetails.indexOf("/to");
            if (fromIndex == -1) {
                throw new NerradException("An event needs a /from start date or time.");
            }
            if (toIndex == -1) {
                throw new NerradException("An event needs a /to end date or time.");
            }
            if (toIndex < fromIndex) {
                throw new NerradException("The /to end date or time must come after /from.");
            }
            String description = taskDetails.substring(0, fromIndex).trim();
            String from = taskDetails.substring(fromIndex + 5, toIndex).trim();
            String to = taskDetails.substring(toIndex + 3).trim();
            if (description.isEmpty()) {
                throw new NerradException("The description of an event cannot be empty.");
            }
            if (from.isEmpty()) {
                throw new NerradException("The /from start date or time of an event cannot be empty.");
            }
            if (to.isEmpty()) {
                throw new NerradException("The /to end date or time of an event cannot be empty.");
            }
            return new Event(description, from, to);
        }

        throw new NerradException("I'm sorry, but I don't know what that means :-(");
    }

    /**
     * Converts a task number in a command into a list index.
     *
     * @param taskNumberText Task number typed by the user.
     * @param taskCount Number of tasks currently stored.
     * @param command Command requesting the task number.
     * @return Zero-based index of the requested task.
     * @throws NerradException If the task number is missing, invalid, or outside the list.
     */
    public int parseTaskIndex(String taskNumberText, int taskCount, String command)
            throws NerradException {
        String trimmedTaskNumber = taskNumberText.trim();
        if (trimmedTaskNumber.isEmpty()) {
            throw new NerradException("Please provide a task number to " + command + ".");
        }

        try {
            int taskNumber = Integer.parseInt(trimmedTaskNumber);
            if (taskNumber < 1 || taskNumber > taskCount) {
                throw new NerradException("There is no task with this number.");
            }
            return taskNumber - 1;
        } catch (NumberFormatException exception) {
            throw new NerradException("The task number must be a whole number.");
        }
    }
}

