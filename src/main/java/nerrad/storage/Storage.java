package nerrad.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import nerrad.task.Deadline;
import nerrad.task.Event;
import nerrad.task.Task;
import nerrad.task.Todo;

/**
 * Saves Nerrad tasks in a text file relative to the application's working directory.
 */
public class Storage {
    /** Location of Nerrad's task data file. */
    private final Path saveFile;

    /**
     * Creates storage that reads from and writes to the given file path.
     *
     * @param filePath Path of Nerrad's task data file.
     */
    public Storage(String filePath) {
        this.saveFile = Path.of(filePath);
    }

    /**
     * Writes every task to the save file, replacing its previous contents.
     *
     * @param tasks Tasks to save.
     * @throws IOException If the data directory or file cannot be written.
     */
    public void saveTasks(List<Task> tasks) throws IOException {
        List<String> taskLines = new ArrayList<>();
        for (Task task : tasks) {
            taskLines.add(formatTask(task));
        }

        Files.createDirectories(saveFile.getParent());
        Files.write(saveFile, taskLines, StandardCharsets.UTF_8);
    }

    /**
     * Loads tasks from the save file. An absent file means Nerrad is being run
     * for the first time, so an empty list is returned.
     *
     * @return Tasks reconstructed from the save file.
     * @throws IOException If an existing save file cannot be read or understood.
     */
    public List<Task> loadTasks() throws IOException {
        if (Files.notExists(saveFile)) {
            return new ArrayList<>();
        }
        if (!Files.isRegularFile(saveFile)) {
            throw new IOException("The save path is not a file.");
        }

        List<Task> tasks = new ArrayList<>();
        for (String taskLine : Files.readAllLines(saveFile, StandardCharsets.UTF_8)) {
            tasks.add(parseTask(taskLine));
        }
        return tasks;
    }

    /**
     * Converts one task into a text line that contains its type, status, and details.
     *
     * @param task Task to format.
     * @return Save-file representation of the task.
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
     * @param taskLine One line from the save file.
     * @return Reconstructed task.
     * @throws IOException If the line is not in the expected format.
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
            try {
                task = new Deadline(parts[2], LocalDate.parse(parts[3]));
            } catch (DateTimeParseException exception) {
                throw new IOException("The save file contains an invalid deadline date.", exception);
            }
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

