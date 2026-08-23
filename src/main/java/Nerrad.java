import java.util.Scanner;

/**
 * Entry point for the Nerrad chatbot.
 */
public class Nerrad {
    /** Command separator between a deadline description and its due date/time. */
    private static final String BY_SEPARATOR = " /by ";

    /** Command separator before an event's start date/time. */
    private static final String FROM_SEPARATOR = " /from ";

    /** Command separator before an event's end date/time. */
    private static final String TO_SEPARATOR = " /to ";

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
                    System.out.println("  " + (i + 1) + ". " + tasks[i]);
                }
                System.out.println(separator);
                continue;
            }

            if (input.startsWith("mark ")) {
                int taskNumber = Integer.parseInt(input.substring(5));
                int taskIndex = taskNumber - 1;
                tasks[taskIndex].markAsDone();
                System.out.println("\n  Nice! I've marked this task as done:");
                System.out.println("    " + tasks[taskIndex]);
                System.out.println(separator);
                continue;
            }

            if (input.startsWith("unmark ")) {
                int taskNumber = Integer.parseInt(input.substring(7));
                int taskIndex = taskNumber - 1;
                tasks[taskIndex].markAsNotDone();
                System.out.println("\n  OK, I've marked this task as not done yet:");
                System.out.println("    " + tasks[taskIndex]);
                System.out.println(separator);
                continue;
            }

            Task newTask = createTask(input);
            tasks[taskCount] = newTask;
            taskCount++;
            System.out.println("\n  Got it. I've added this task:");
            System.out.println("    " + newTask);
            System.out.println("  Now you have " + taskCount + " tasks in the list.");
            System.out.println(separator);
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
    private static Task createTask(String input) {
        if (input.startsWith("todo ")) {
            return new Todo(input.substring(5));
        }

        if (input.startsWith("deadline ")) {
            String taskDetails = input.substring(9);
            int byIndex = taskDetails.indexOf(BY_SEPARATOR);
            String description = taskDetails.substring(0, byIndex);
            String by = taskDetails.substring(byIndex + BY_SEPARATOR.length());
            return new Deadline(description, by);
        }

        if (input.startsWith("event ")) {
            String taskDetails = input.substring(6);
            int fromIndex = taskDetails.indexOf(FROM_SEPARATOR);
            int toIndex = taskDetails.indexOf(TO_SEPARATOR, fromIndex + FROM_SEPARATOR.length());
            String description = taskDetails.substring(0, fromIndex);
            String from = taskDetails.substring(fromIndex + FROM_SEPARATOR.length(), toIndex);
            String to = taskDetails.substring(toIndex + TO_SEPARATOR.length());
            return new Event(description, from, to);
        }

        // Preserve the earlier behavior where plain text is stored as a task.
        return new Todo(input);
    }
}
