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

    public TaskTools(){
        try {
            service = GoogleTasksService.getService();
        } catch (Exception e) {
            System.err.println("Failed to create Google Tasks service:");
            e.printStackTrace();
        }
    }

    public void addTask(LocalDateTime dateTime, String task) {
        Task newTask = new Task()
                .setTitle(task);

        if(dateTime != null){
            String dueDateString = dateTime.atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            newTask.setDue(dueDateString);
        }

        try {
            service.tasks().insert("@default", newTask).execute();
        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Task created: " + task + " due " + dateTime);
    }

    public void updateTask(String oldTaskTitle, String newTitle, LocalDateTime newDateTime) {
        System.out.println("Task updated: " + newTitle);
        String taskId = getTaskId(oldTaskTitle);
        String dueDateString = newDateTime.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        try {
            Task task = service.tasks().get("@default", taskId).execute();

            task.setTitle(newTitle);
            task.setDue(dueDateString);

            service.tasks().update("@default", taskId, task).execute();

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

    private List<Task> getTasks() {
        try {
             return service.tasks().list("@default").execute().getItems();
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

    public List<String> getTasksToString(){
        try {
            List<Task> list = service.tasks().list("@default").execute().getItems();
            List<String> tasks = list.stream().map(Task::getTitle).toList();
            return tasks;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
