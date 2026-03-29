package pl.clockworkjava.tasks;

import java.time.LocalDateTime;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import pl.clockworkjava.GoogleTasksService;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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

    public void updateTask(LocalDateTime dateTime, String task){

    }

    public void deleteTask(LocalDateTime dateTime, String task){

    }

    public void getTasks(){

    }
}
