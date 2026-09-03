package nerrad.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores tasks and provides the operations used to manage the task list.
 */
public class TaskList {
    /** Tasks currently managed by Nerrad. */
    private final List<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list containing the supplied saved tasks.
     *
     * @param tasks Tasks to manage.
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Returns the number of tasks in this list.
     *
     * @return Number of managed tasks.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns the task at a zero-based index.
     *
     * @param index Zero-based task index.
     * @return Requested task.
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Returns a read-only view of the managed tasks for display or saving.
     *
     * @return Unmodifiable task list view.
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Returns tasks whose descriptions contain the given keyword in their current order.
     *
     * @param keyword Keyword to search for.
     * @return Matching tasks.
     */
    public List<Task> findTasks(String keyword) {
        List<Task> matchingTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getDescription().contains(keyword)) {
                matchingTasks.add(task);
            }
        }
        return matchingTasks;
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task Task to add.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Inserts a task at a zero-based index.
     *
     * @param index Insertion index.
     * @param task Task to insert.
     */
    public void add(int index, Task task) {
        tasks.add(index, task);
    }

    /**
     * Removes and returns the task at a zero-based index.
     *
     * @param index Index of the task to remove.
     * @return Removed task.
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Changes the completion state of a task.
     *
     * @param index Zero-based index of the task to update.
     * @param shouldBeDone Whether the task should be marked done.
     */
    public void setDone(int index, boolean shouldBeDone) {
        Task task = get(index);
        if (shouldBeDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
    }
}

