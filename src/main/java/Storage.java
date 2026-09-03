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
}
