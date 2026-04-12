package pl.clockworkjava.aiTools;

import java.time.LocalDateTime;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import pl.clockworkjava.service.GoogleTasksService;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class TaskTools {

    private Tasks service;

    public TaskTools() {
        try {
            service = GoogleTasksService.getService();
        } catch (Exception e) {
            System.err.println("Failed to create Google Tasks service:");
            e.printStackTrace();
        }
    }

    public void addTask(LocalDateTime dateTime, String task) {
        if (!isServiceAvailable() || task == null || task.isBlank()) {
            System.out.println("Task was not created because the input was incomplete.");
            return;
        }

        Task newTask = new Task()
                .setTitle(task);

        if (dateTime != null) {
            String dueDateString = dateTime.atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            newTask.setDue(dueDateString);
        }

        try {
            service.tasks().insert("@default", newTask).execute();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Task created: " + task + " due " + dateTime);
    }

    public void updateTask(String oldTaskTitle, String newTitle, LocalDateTime newDateTime) {
        if (!isServiceAvailable()) {
            return;
        }
        if (oldTaskTitle == null || oldTaskTitle.isBlank() || newTitle == null || newTitle.isBlank()) {
            System.out.println("Task update skipped because the task title was missing.");
            return;
        }

        System.out.println("Task updated: " + newTitle);
        String taskId = getTaskId(oldTaskTitle);
        if (taskId == null) {
            System.out.println("Task update skipped because the original task was not found.");
            return;
        }

        try {
            Task task = service.tasks().get("@default", taskId).execute();

            task.setTitle(newTitle);
            if (newDateTime != null) {
                String dueDateString = newDateTime.atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                task.setDue(dueDateString);
            }

            service.tasks().update("@default", taskId, task).execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteTask(String taskTitle) {
        if (!isServiceAvailable() || taskTitle == null || taskTitle.isBlank()) {
            System.out.println("Task delete skipped because the task title was missing.");
            return;
        }

        String taskId = getTaskId(taskTitle);
        if (taskId == null) {
            System.out.println("Task delete skipped because the task was not found.");
            return;
        }

        try {
            service.tasks().delete("@default", taskId).execute();
            System.out.println("Task deleted: " + taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Task> getTasks() {
        if (!isServiceAvailable()) {
            return Collections.emptyList();
        }

        try {
            List<Task> items = service.tasks().list("@default").execute().getItems();
            return items == null ? Collections.emptyList() : items;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private String getTaskId(String taskTitle) {
        try {
            List<Task> tasks = getTasks();
                for (Task t : tasks) {
                    if (t.getTitle().equalsIgnoreCase(taskTitle)) {
                        return t.getId();
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getTasksToString() {
        if (!isServiceAvailable()) {
            return Collections.emptyList();
        }

        try {
            List<Task> list = service.tasks().list("@default").execute().getItems();
            if (list == null) {
                return Collections.emptyList();
            }
            return list.stream().map(Task::getTitle).toList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private boolean isServiceAvailable() {
        if (service != null) {
            return true;
        }

        System.out.println("Google Tasks service is unavailable.");
        return false;
    }
}
