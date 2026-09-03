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
        Ui ui = new Ui();
        Parser parser = new Parser();
        List<Task> tasks;
        try {
            tasks = loadTasks();
        } catch (NerradException exception) {
            ui.showLoadingError();
            return;
        }

        ui.showWelcome();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = ui.readCommand(scanner);
            if (input == null) {
                break;
            }

            if (input.equals("bye")) {
                ui.showGoodbye();
                break;
            }

            if (input.equals("list")) {
                ui.showTaskList(tasks);
                continue;
            }

            try {
                if (input.equals("mark") || input.startsWith("mark ")) {
                    int taskIndex = parser.parseTaskIndex(input.substring(4), tasks.size(), "mark");
                    setTaskDone(tasks, taskIndex, true);
                    ui.showTaskMarked(tasks.get(taskIndex));
                    continue;
                }

                if (input.equals("unmark") || input.startsWith("unmark ")) {
                    int taskIndex = parser.parseTaskIndex(input.substring(6), tasks.size(), "unmark");
                    setTaskDone(tasks, taskIndex, false);
                    ui.showTaskUnmarked(tasks.get(taskIndex));
                    continue;
                }

                if (input.equals("delete") || input.startsWith("delete ")) {
                    int taskIndex = parser.parseTaskIndex(input.substring(6), tasks.size(), "delete");
                    Task deletedTask = deleteTask(tasks, taskIndex);
                    ui.showTaskDeleted(deletedTask, tasks.size());
                    continue;
                }

                Task newTask = parser.parseTask(input);
                addTask(tasks, newTask);
                ui.showTaskAdded(newTask, tasks.size());
            } catch (NerradException exception) {
                ui.showError(exception.getMessage());
            }
        }
        scanner.close();
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
