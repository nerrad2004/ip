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
     * @param filePath Path of the task data file.
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
            ui.showLoadingError();
            return;
        }

        ui.showWelcome();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String input = ui.readCommand(scanner);
                if (input == null) {
                    break;
                }

                ui.showResponse(getResponse(input));
                if (isExitCommand(input)) {
                    break;
                }
            }
        }
    }

    /**
     * Returns the chatbot's reply after processing a user command.
     *
     * @param input Command entered by the user.
     * @return Reply to display to the user.
     */
    public String getResponse(String input) {
        if (hasLoadingError) {
            return ui.getLoadingErrorMessage();
        }

        if (isExitCommand(input)) {
            return ui.getGoodbyeMessage();
        }

        if (input.equals("list")) {
            return ui.getTaskListMessage(tasks.getTasks());
        }

        try {
            if (input.equals("find") || input.startsWith("find ")) {
                String keyword = input.substring(4).trim();
                if (keyword.isEmpty()) {
                    throw new NerradException("Please provide a keyword to find.");
                }
                return ui.getMatchingTasksMessage(tasks.findTasks(keyword));
            }

            if (input.equals("mark") || input.startsWith("mark ")) {
                int taskIndex = parser.parseTaskIndex(input.substring(4), tasks.size(), "mark");
                setTaskDone(taskIndex, true);
                return ui.getTaskMarkedMessage(tasks.get(taskIndex));
            }

            if (input.equals("unmark") || input.startsWith("unmark ")) {
                int taskIndex = parser.parseTaskIndex(input.substring(6), tasks.size(), "unmark");
                setTaskDone(taskIndex, false);
                return ui.getTaskUnmarkedMessage(tasks.get(taskIndex));
            }

            if (input.equals("delete") || input.startsWith("delete ")) {
                int taskIndex = parser.parseTaskIndex(input.substring(6), tasks.size(), "delete");
                Task deletedTask = deleteTask(taskIndex);
                return ui.getTaskDeletedMessage(deletedTask, tasks.size());
            }

            Task newTask = parser.parseTask(input);
            addTask(newTask);
            return ui.getTaskAddedMessage(newTask, tasks.size());
        } catch (NerradException exception) {
            return ui.getErrorMessage(exception.getMessage());
        }
    }

    /**
     * Returns Nerrad's greeting for graphical user interfaces.
     *
     * @return Startup greeting, or a loading error when saved data is invalid.
     */
    public String getWelcomeMessage() {
        return hasLoadingError ? ui.getLoadingErrorMessage() : ui.getWelcomeMessage();
    }

    /**
     * Returns whether a command asks Nerrad to exit.
     *
     * @param input Command entered by the user.
     * @return Whether the command is {@code bye}.
     */
    public boolean isExitCommand(String input) {
        return input.equals("bye");
    }

    /**
     * Returns whether Nerrad could not load its saved tasks at startup.
     *
     * @return Whether task loading failed.
     */
    public boolean hasLoadingError() {
        return hasLoadingError;
    }

    /**
     * Starts Nerrad using the normal project-relative data file.
     *
     * @param args Command-line arguments; not used.
     */
    public static void main(String[] args) {
        new Nerrad("data/nerrad.txt").run();
    }

    /**
     * Saves the current task list and converts file-writing failures into a chatbot error.
     *
     * @throws NerradException If the task list cannot be saved.
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
     * @param newTask Task to add.
     * @throws NerradException If the changed list cannot be saved.
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
     * @param taskIndex Index of the task to change.
     * @param shouldBeDone Desired completion status.
     * @throws NerradException If the changed list cannot be saved.
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
     * @param taskIndex Index of the task to delete.
     * @return Deleted task.
     * @throws NerradException If the changed list cannot be saved.
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
