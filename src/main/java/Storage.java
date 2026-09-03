import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves Nerrad tasks in a text file relative to the application's working directory.
 */
public class Storage {
    /** Location of Nerrad's task data file. */
    private static final Path SAVE_FILE = Path.of("data", "nerrad.txt");

    /**
     * Writes every task to the save file, replacing its previous contents.
     *
     * @param tasks tasks to save
     * @throws IOException if the data directory or file cannot be written
     */
    public static void saveTasks(List<Task> tasks) throws IOException {
        List<String> taskLines = new ArrayList<>();
        for (Task task : tasks) {
            taskLines.add(formatTask(task));
        }

        Files.createDirectories(SAVE_FILE.getParent());
        Files.write(SAVE_FILE, taskLines, StandardCharsets.UTF_8);
    }

    /**
     * Loads tasks from the save file. An absent file means Nerrad is being run
     * for the first time, so an empty list is returned.
     *
     * @return tasks reconstructed from the save file
     * @throws IOException if an existing save file cannot be read or understood
     */
    public static List<Task> loadTasks() throws IOException {
        if (Files.notExists(SAVE_FILE)) {
            return new ArrayList<>();
        }

        List<Task> tasks = new ArrayList<>();
        for (String taskLine : Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8)) {
            tasks.add(parseTask(taskLine));
        }
        return tasks;
    }

    /**
     * Converts one task into a text line that contains its type, status, and details.
     *
     * @param task task to format
     * @return save-file representation of the task
     */
    private static String formatTask(Task task) {
        String doneStatus = task.isDone() ? "1" : "0";
        if (task instanceof Todo) {
            return "T | " + doneStatus + " | " + task.getDescription();
        }
        if (task instanceof Deadline) {
            Deadline deadline = (Deadline) task;
            return "D | " + doneStatus + " | " + deadline.getDescription()
                    + " | " + deadline.getBy();
        }

        Event event = (Event) task;
        return "E | " + doneStatus + " | " + event.getDescription()
                + " | " + event.getFrom() + " | " + event.getTo();
    }

    /**
     * Reconstructs a task saved in Nerrad's text-file format.
     *
     * @param taskLine one line from the save file
     * @return reconstructed task
     * @throws IOException if the line is not in the expected format
     */
    private static Task parseTask(String taskLine) throws IOException {
        String[] parts = taskLine.split(" \\| ", -1);
        if (parts.length < 3 || (!parts[1].equals("0") && !parts[1].equals("1"))) {
            throw new IOException("The save file contains an invalid task.");
        }

        Task task;
        switch (parts[0]) {
        case "T":
            if (parts.length != 3) {
                throw new IOException("The save file contains an invalid todo.");
            }
            task = new Todo(parts[2]);
            break;
        case "D":
            if (parts.length != 4) {
                throw new IOException("The save file contains an invalid deadline.");
            }
            task = new Deadline(parts[2], parts[3]);
            break;
        case "E":
            if (parts.length != 5) {
                throw new IOException("The save file contains an invalid event.");
            }
            task = new Event(parts[2], parts[3], parts[4]);
            break;
        default:
            throw new IOException("The save file contains an unknown task type.");
        }

        if (parts[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }
}
