import java.util.Scanner;

/**
 * Entry point for the Nerrad chatbot.
 */
public class Nerrad {
    /**
     * Starts the chatbot and processes commands until the user enters {@code bye}.
     *
     * @param args command-line arguments; not used
     */
    public static void main(String[] args) {
        String separator = "____________________________________________________________";
        String banner = " _   _                         _ \n"
                + "| \\ | | ___ _ __ _ __ __ _  __| |\n"
                + "|  \\| |/ _ \\ '__| '__/ _` |/ _` |\n"
                + "| |\\  |  __/ |  | | | (_| | (_| |\n"
                + "|_| \\_|\\___|_|  |_|  \\__,_|\\__,_|\n";

        System.out.println(separator);
        System.out.print(banner);
        System.out.println("\nHello! I'm Nerrad. My actual name is that backwards! xD");
        System.out.println("What can I do for you?");
        System.out.println(separator);

        Task[] tasks = new Task[100];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine();

            if (input.equals("bye")) {
                System.out.println("\n  Bye! Hope to see you again soon!!!");
                System.out.println(separator);
                break;
            }

            if (input.equals("list")) {
                System.out.println("\n  Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println("  " + (i + 1) + "." + tasks[i]);
                }
                System.out.println(separator);
                continue;
            }

            try {
                if (input.equals("mark") || input.startsWith("mark ")) {
                    int taskIndex = getTaskIndex(input.substring(4), taskCount, "mark");
                    tasks[taskIndex].markAsDone();
                    System.out.println("\n  Nice! I've marked this task as done:");
                    System.out.println("    " + tasks[taskIndex]);
                    System.out.println(separator);
                    continue;
                }

                if (input.equals("unmark") || input.startsWith("unmark ")) {
                    int taskIndex = getTaskIndex(input.substring(6), taskCount, "unmark");
                    tasks[taskIndex].markAsNotDone();
                    System.out.println("\n  OK, I've marked this task as not done yet:");
                    System.out.println("    " + tasks[taskIndex]);
                    System.out.println(separator);
                    continue;
                }

                if (taskCount == tasks.length) {
                    throw new NerradException("Your task list is full. Please delete a task before adding another.");
                }

                Task newTask = createTask(input);
                tasks[taskCount] = newTask;
                taskCount++;
                System.out.println("\n  Got it. I've added this task:");
                System.out.println("    " + newTask);
                System.out.println("  Now you have " + taskCount + " tasks in the list.");
                System.out.println(separator);
            } catch (NerradException exception) {
                System.out.println("\n  OOPS!!! " + exception.getMessage());
                System.out.println(separator);
            }
        }
        scanner.close();
    }

    /**
     * Creates the task subtype specified by a valid user command.
     * Date and time information is kept as entered because date parsing is not
     * required at this level.
     *
     * @param input command entered by the user
     * @return a todo, deadline, or event represented as a {@link Task}
     */
    private static Task createTask(String input) throws NerradException {
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
            String by = taskDetails.substring(byIndex + 3).trim();
            if (description.isEmpty()) {
                throw new NerradException("The description of a deadline cannot be empty.");
            }
            if (by.isEmpty()) {
                throw new NerradException("The /by date or time of a deadline cannot be empty.");
            }
            return new Deadline(description, by);
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
     * Converts a task number in a mark or unmark command into an array index.
     *
     * @param taskNumberText task number typed by the user
     * @param taskCount number of tasks currently stored
     * @param command command requesting the task number
     * @return zero-based index of the requested task
     * @throws NerradException if the task number is missing, invalid, or outside the list
     */
    private static int getTaskIndex(String taskNumberText, int taskCount, String command)
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
