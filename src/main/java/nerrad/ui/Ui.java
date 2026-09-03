package nerrad.ui;

import java.util.List;
import java.util.Scanner;

import nerrad.task.Task;

/**
 * Handles all console input and output for the Nerrad chatbot.
 */
public class Ui {
    /** Horizontal line used to separate chatbot messages. */
    private static final String SEPARATOR = "____________________________________________________________";

    /** ASCII-art banner displayed when the chatbot starts. */
    private static final String BANNER = " _   _                         _ \n"
            + "| \\ | | ___ _ __ _ __ __ _  __| |\n"
            + "|  \\| |/ _ \\ '__| '__/ _` |/ _` |\n"
            + "| |\\  |  __/ |  | | | (_| | | (_| |\n"
            + "|_| \\_|\\___|_|  |_|  \\__,_|\\__,_|\n";

    /** Displays the chatbot's startup banner and greeting. */
    public void showWelcome() {
        System.out.println(SEPARATOR);
        System.out.print(BANNER);
        System.out.println("\nHello! I'm Nerrad. My actual name is that backwards! xD");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);
    }

    /**
     * Shows the input prompt and reads one command from the console.
     *
     * @param scanner Scanner connected to standard input.
     * @return The entered command, or {@code null} when there is no more input.
     */
    public String readCommand(Scanner scanner) {
        System.out.print("You: ");
        System.out.flush();
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    /** Displays the farewell message. */
    public void showGoodbye() {
        System.out.println("\n  Bye! Hope to see you again soon!!!");
        System.out.println(SEPARATOR);
    }

    /**
     * Displays every task currently stored in the task list.
     *
     * @param tasks Tasks to display.
     */
    public void showTaskList(List<Task> tasks) {
        System.out.println("\n  Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println("  " + (i + 1) + "." + tasks.get(i));
        }
        System.out.println(SEPARATOR);
    }

    /**
     * Displays confirmation that a task was added.
     *
     * @param task Task that was added.
     * @param taskCount Updated number of tasks.
     */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println("\n  Got it. I've added this task:");
        System.out.println("    " + task);
        System.out.println("  Now you have " + taskCount + " tasks in the list.");
        System.out.println(SEPARATOR);
    }

    /**
     * Displays confirmation that a task was marked as done.
     *
     * @param task Task that was marked.
     */
    public void showTaskMarked(Task task) {
        System.out.println("\n  Nice! I've marked this task as done:");
        System.out.println("    " + task);
        System.out.println(SEPARATOR);
    }

    /**
     * Displays confirmation that a task was marked as not done.
     *
     * @param task Task that was unmarked.
     */
    public void showTaskUnmarked(Task task) {
        System.out.println("\n  OK, I've marked this task as not done yet:");
        System.out.println("    " + task);
        System.out.println(SEPARATOR);
    }

    /**
     * Displays confirmation that a task was deleted.
     *
     * @param task Task that was deleted.
     * @param taskCount Updated number of tasks.
     */
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println("\n  Noted. I've removed this task:");
        System.out.println("    " + task);
        System.out.println("  Now you have " + taskCount + " tasks in the list.");
        System.out.println(SEPARATOR);
    }

    /**
     * Displays a user-facing error message.
     *
     * @param message Explanation of the problem.
     */
    public void showError(String message) {
        System.out.println("\n  OOPS!!! " + message);
        System.out.println(SEPARATOR);
    }

    /** Displays the error used when saved data cannot be loaded. */
    public void showLoadingError() {
        System.out.println("  OOPS!!! I could not load your saved tasks.");
        System.out.println(SEPARATOR);
    }
}

