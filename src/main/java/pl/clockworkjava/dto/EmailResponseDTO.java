package pl.clockworkjava.dto;

import pl.clockworkjava.model.Task;

import java.util.ArrayList;
import java.util.List;

public class EmailResponseDTO {
    public String response;
    public List<Task> tasks = new ArrayList<>();

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
        this.tasks = tasks;
    }
}
