package pl.clockworkjava.dto;

import pl.clockworkjava.model.Task;

import java.util.ArrayList;
import java.util.List;

public class EmailResponseDTO {

    private String response;
    private List<Task> tasks = new ArrayList<>();

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks == null ? new ArrayList<>() : new ArrayList<>(tasks);
    }

    public void addTask(Task task) {
        if (task != null) {
            tasks.add(task);
        }
    }
}
