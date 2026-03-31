package pl.clockworkjava.tasks;

import java.time.LocalDateTime;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import pl.clockworkjava.GoogleTasksService;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TasksBackend {

    private Tasks service;

    TasksBackend(){
        try {
            service = GoogleTasksService.getService();
        } catch (Exception e) {
            System.err.println("Failed to create Google Tasks service:");
            e.printStackTrace();
        }
    }

    public void addTask(LocalDateTime dateTime, String task) {
        String dueDateString = dateTime.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Task newTask = new Task()
                .setTitle(task)
                .setDue(dueDateString);

        try {
            service.tasks().insert("@default", newTask).execute();
        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Task created: " + task + " due " + dateTime);
    }

    public void updateTask(String oldTaskTitle, String newTitle, LocalDateTime newDateTime) {
        String taskId = getTaskId(oldTaskTitle);
        String dueDateString = newDateTime.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        try {
            Task task = service.tasks().get("@default", taskId).execute();

            task.setTitle(newTitle);
            task.setDue(dueDateString);

            service.tasks().update("@default", taskId, task).execute();

            System.out.println("Task updated: " + newTitle);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteTask(String taskTitle) {
        String taskId = getTaskId(taskTitle);
        try {
            service.tasks().delete("@default", taskId).execute();
            System.out.println("Task deleted: " + taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Task> getTasks() {
        try {
            return service.tasks().list("@default").execute().getItems();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
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
}
