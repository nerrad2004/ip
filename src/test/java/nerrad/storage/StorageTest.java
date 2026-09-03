package nerrad.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nerrad.task.Deadline;
import nerrad.task.Event;
import nerrad.task.Task;
import nerrad.task.Todo;

/**
 * Tests persistence of tasks through {@link Storage}.
 */
class StorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void loadTasks_missingSaveFile_returnsEmptyList() throws IOException {
        Storage storage = createStorage();

        assertTrue(storage.loadTasks().isEmpty());
    }

    @Test
    void saveTasksAndLoadTasks_mixedTasks_roundTripsAllDetailsAndStatuses() throws IOException {
        List<Task> tasks = new ArrayList<>();
        Todo completedTodo = new Todo("read book");
        completedTodo.markAsDone();
        tasks.add(completedTodo);
        tasks.add(new Deadline("return book", LocalDate.of(2019, 12, 2)));
        tasks.add(new Event("project meeting", "Mon 2pm", "4pm"));

        Storage storage = createStorage();
        storage.saveTasks(tasks);
        List<Task> loadedTasks = storage.loadTasks();

        assertEquals(3, loadedTasks.size());
        assertEquals("[T][X] read book", loadedTasks.get(0).toString());
        assertEquals("[D][ ] return book (by: Dec 02 2019)", loadedTasks.get(1).toString());
        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 4pm)", loadedTasks.get(2).toString());
    }

    @Test
    void loadTasks_corruptedSaveData_throwsIOException() throws IOException {
        Path saveFile = temporaryDirectory.resolve("data").resolve("nerrad.txt");
        Files.createDirectories(saveFile.getParent());
        Storage storage = new Storage(saveFile.toString());

        for (String invalidTaskLine : List.of(
                "Z | 0 | unknown task",
                "T | 2 | invalid status",
                "D | 0 | invalid date | tomorrow")) {
            Files.writeString(saveFile, invalidTaskLine);

            assertThrows(IOException.class, storage::loadTasks);
        }
    }

    /**
     * Creates storage with a fresh nested data path, exercising directory creation on save.
     *
     * @return storage for this test's temporary save file
     */
    private Storage createStorage() {
        return new Storage(temporaryDirectory.resolve("data").resolve("nerrad.txt").toString());
    }
}
