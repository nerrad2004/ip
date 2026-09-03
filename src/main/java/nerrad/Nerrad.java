package nerrad;

import java.io.IOException;
import java.util.Scanner;

import nerrad.parser.Parser;
import nerrad.storage.Storage;
import nerrad.task.Task;
import nerrad.task.TaskList;
import nerrad.ui.Ui;

/**
 * Coordinates Nerrad's user interface, command parser, storage, and task list.
 */
public class Nerrad {
    /** User interface used to read commands and display responses. */
    private final Ui ui;

    /** Parser used to interpret task details and task numbers. */
    private final Parser parser;

    /** Storage used to load and save this chatbot's task data. */
    private final Storage storage;

    /** Tasks managed during this chatbot session. */
    private final TaskList tasks;

    /** Whether startup failed because saved tasks could not be loaded. */
    private final boolean hasLoadingError;

    /**
     * Creates a Nerrad chatbot that stores its tasks at the given path.
     *
     * @param filePath path of the task data file
     */
    public Nerrad(String filePath) {
        this.ui = new Ui();
        this.parser = new Parser();
        this.storage = new Storage(filePath);

        TaskList loadedTasks;
        boolean loadingFailed = false;
        try {
            loadedTasks = new TaskList(storage.loadTasks());
        } catch (IOException exception) {
            ui.showLoadingError();
            loadedTasks = new TaskList();
            loadingFailed = true;
        }
        this.tasks = loadedTasks;
        this.hasLoadingError = loadingFailed;
    }

    /**
     * Starts the chatbot and processes commands until the user enters bye.
     */
    public void run() {
        if (hasLoadingError) {
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
                ui.showTaskList(tasks.getTasks());
                continue;
            }

            try {
                if (input.equals("mark") || input.startsWith("mark ")) {
                    int taskIndex = parser.parseTaskIndex(input.substring(4), tasks.size(), "mark");
                    setTaskDone(taskIndex, true);
                    ui.showTaskMarked(tasks.get(taskIndex));
                    continue;
                }

                if (input.equals("unmark") || input.startsWith("unmark ")) {
                    int taskIndex = parser.parseTaskIndex(input.substring(6), tasks.size(), "unmark");
                    setTaskDone(taskIndex, false);
                    ui.showTaskUnmarked(tasks.get(taskIndex));
                    continue;
                }

                if (input.equals("delete") || input.startsWith("delete ")) {
                    int taskIndex = parser.parseTaskIndex(input.substring(6), tasks.size(), "delete");
                    Task deletedTask = deleteTask(taskIndex);
                    ui.showTaskDeleted(deletedTask, tasks.size());
                    continue;
                }

                Task newTask = parser.parseTask(input);
                addTask(newTask);
                ui.showTaskAdded(newTask, tasks.size());
            } catch (NerradException exception) {
                ui.showError(exception.getMessage());
            }
        }
        scanner.close();
    }

    /**
     * Starts Nerrad using the normal project-relative data file.
     *
     * @param args command-line arguments; not used
     */
    public static void main(String[] args) {
        new Nerrad("data/nerrad.txt").run();
    }

    /**
     * Saves the current task list and converts file-writing failures into a chatbot error.
     *
     * @throws NerradException if the task list cannot be saved
     */
    private void saveTasks() throws NerradException {
        try {
            storage.saveTasks(tasks.getTasks());
        } catch (IOException exception) {
            throw new NerradException("I could not save your tasks.");
        }
    }

    /**
     * Adds a task only if the changed list can be saved successfully.
     *
     * @param newTask task to add
     * @throws NerradException if the changed list cannot be saved
     */
    private void addTask(Task newTask) throws NerradException {
        tasks.add(newTask);
        try {
            saveTasks();
        } catch (NerradException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
    }

    /**
     * Changes a task's completion status only if the changed list can be saved.
     *
     * @param taskIndex index of the task to change
     * @param shouldBeDone desired completion status
     * @throws NerradException if the changed list cannot be saved
     */
    private void setTaskDone(int taskIndex, boolean shouldBeDone) throws NerradException {
        boolean wasDone = tasks.get(taskIndex).isDone();
        tasks.setDone(taskIndex, shouldBeDone);

        try {
            saveTasks();
        } catch (NerradException exception) {
            tasks.setDone(taskIndex, wasDone);
            throw exception;
        }
    }

    /**
     * Deletes a task only if the changed list can be saved successfully.
     *
     * @param taskIndex index of the task to delete
     * @return deleted task
     * @throws NerradException if the changed list cannot be saved
     */
    private Task deleteTask(int taskIndex) throws NerradException {
        Task deletedTask = tasks.remove(taskIndex);
        try {
            saveTasks();
            return deletedTask;
        } catch (NerradException exception) {
            tasks.add(taskIndex, deletedTask);
            throw exception;
        }
    }
}
