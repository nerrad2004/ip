import java.io.IOException;
import java.util.List;
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
        List<Task> tasks;
        try {
            tasks = loadTasks();
        } catch (NerradException exception) {
            System.out.println("  OOPS!!! " + exception.getMessage());
            System.out.println(separator);
            return;
        }

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

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            System.out.print("You: ");
            String input = scanner.nextLine();

            if (input.equals("bye")) {
                System.out.println("\n  Bye! Hope to see you again soon!!!");
                System.out.println(separator);
                break;
            }

            if (input.equals("list")) {
                System.out.println("\n  Here are the tasks in your list:");
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println("  " + (i + 1) + "." + tasks.get(i));
                }
                System.out.println(separator);
                continue;
            }

            try {
                if (input.equals("mark") || input.startsWith("mark ")) {
                    int taskIndex = getTaskIndex(input.substring(4), tasks.size(), "mark");
                    setTaskDone(tasks, taskIndex, true);
                    System.out.println("\n  Nice! I've marked this task as done:");
                    System.out.println("    " + tasks.get(taskIndex));
                    System.out.println(separator);
                    continue;
                }

                if (input.equals("unmark") || input.startsWith("unmark ")) {
                    int taskIndex = getTaskIndex(input.substring(6), tasks.size(), "unmark");
                    setTaskDone(tasks, taskIndex, false);
                    System.out.println("\n  OK, I've marked this task as not done yet:");
                    System.out.println("    " + tasks.get(taskIndex));
                    System.out.println(separator);
                    continue;
                }

                if (input.equals("delete") || input.startsWith("delete ")) {
                    int taskIndex = getTaskIndex(input.substring(6), tasks.size(), "delete");
                    Task deletedTask = deleteTask(tasks, taskIndex);
                    System.out.println("\n  Noted. I've removed this task:");
                    System.out.println("    " + deletedTask);
                    System.out.println("  Now you have " + tasks.size() + " tasks in the list.");
                    System.out.println(separator);
                    continue;
                }

                Task newTask = createTask(input);
                addTask(tasks, newTask);
                System.out.println("\n  Got it. I've added this task:");
                System.out.println("    " + newTask);
                System.out.println("  Now you have " + tasks.size() + " tasks in the list.");
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
     * Converts a task number in a command into a list index.
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

    /**
     * Saves the current task list and converts file-writing failures into a chatbot error.
     *
     * @param tasks tasks to save
     * @throws NerradException if the task list cannot be saved
     */
    private static void saveTasks(List<Task> tasks) throws NerradException {
        try {
            Storage.saveTasks(tasks);
        } catch (IOException exception) {
            throw new NerradException("I could not save your tasks.");
        }
    }

    /**
     * Adds a task only if the changed list can be saved successfully.
     *
     * @param tasks tasks currently in the list
     * @param newTask task to add
     * @throws NerradException if the changed list cannot be saved
     */
    private static void addTask(List<Task> tasks, Task newTask) throws NerradException {
        tasks.add(newTask);
        try {
            saveTasks(tasks);
        } catch (NerradException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
    }

    /**
     * Changes a task's completion status only if the changed list can be saved.
     *
     * @param tasks tasks currently in the list
     * @param taskIndex index of the task to change
     * @param shouldBeDone desired completion status
     * @throws NerradException if the changed list cannot be saved
     */
    private static void setTaskDone(List<Task> tasks, int taskIndex, boolean shouldBeDone)
            throws NerradException {
        Task task = tasks.get(taskIndex);
        boolean wasDone = task.isDone();
        if (shouldBeDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }

        try {
            saveTasks(tasks);
        } catch (NerradException exception) {
            if (wasDone) {
                task.markAsDone();
            } else {
                task.markAsNotDone();
            }
            throw exception;
        }
    }

    /**
     * Deletes a task only if the changed list can be saved successfully.
     *
     * @param tasks tasks currently in the list
     * @param taskIndex index of the task to delete
     * @return deleted task
     * @throws NerradException if the changed list cannot be saved
     */
    private static Task deleteTask(List<Task> tasks, int taskIndex) throws NerradException {
        Task deletedTask = tasks.remove(taskIndex);
        try {
            saveTasks(tasks);
            return deletedTask;
        } catch (NerradException exception) {
            tasks.add(taskIndex, deletedTask);
            throw exception;
        }
    }

    /**
     * Loads saved tasks and converts file-reading failures into a chatbot error.
     *
     * @return tasks saved by a previous run of Nerrad
     * @throws NerradException if the saved task list cannot be loaded
     */
    private static List<Task> loadTasks() throws NerradException {
        try {
            return Storage.loadTasks();
        } catch (IOException exception) {
            throw new NerradException("I could not load your saved tasks.");
        }
    }
}
