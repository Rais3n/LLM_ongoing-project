package pl.clockworkjava.service;

import org.json.JSONArray;
import org.json.JSONObject;
import pl.clockworkjava.AppConfig;
import pl.clockworkjava.PromptProvider;
import pl.clockworkjava.aiTools.TaskTools;
import pl.clockworkjava.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TaskService {

    private List<Task> tasks;

    private final TaskTools taskTools = new TaskTools();
    private final String hfUrl = AppConfig.CHAT_COMPLETIONS_URL;
    private final String hfToken = AppConfig.getHfToken();
    private final String model = AppConfig.DEFAULT_CHAT_MODEL;

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks == null ? Collections.emptyList() : tasks;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public boolean hasPendingTasks() {
        return tasks != null && !tasks.isEmpty();
    }

    private Task convertJsonToTask(JSONObject jsonTask) {
        Task task = new Task();
        JSONObject parametersJSON = jsonTask.optJSONObject("arguments");
        if (parametersJSON == null) {
            return task;
        }

        task.setDateTime(parseDateTime(parametersJSON.optString("date", null)));

        String taskName = parametersJSON.optString("taskName", "").trim();
        if (!taskName.isEmpty()) {
            task.setName(taskName);
            return task;
        }

        JSONArray taskNames = parametersJSON.optJSONArray("taskNames");
        if (taskNames != null && taskNames.length() > 0) {
            task.setOldName(taskNames.optString(0, null));
            if (taskNames.length() > 1) {
                task.setName(taskNames.optString(1, ""));
            } else {
                task.setName(taskNames.optString(0, ""));
            }
        }

        return task;
    }

    public void respond(String userMessage) {
        String tool = callTool(userMessage);
        System.out.println(tool);

        if (tool == null || tool.isBlank()) {
            System.out.println("I couldn't understand the task action.");
            return;
        }

        JSONObject json;
        try {
            json = new JSONObject(tool);
        } catch (Exception e) {
            System.out.println("The task action response was not valid JSON.");
            return;
        }

        JSONArray tools = json.optJSONArray("tools");
        if (tools == null || tools.isEmpty()) {
            System.out.println("No task actions were returned.");
            return;
        }

        for (int i = 0; i < tools.length(); i++) {
            JSONObject actionJSON = tools.optJSONObject(i);
            if (actionJSON == null) {
                continue;
            }

            String methodName = actionJSON.optString("name", "").trim();
            if (methodName.isEmpty()) {
                System.out.println("Skipped a task action with no name.");
                continue;
            }

            Task task = convertJsonToTask(actionJSON);

            switch (methodName) {
                case "addTask":
                    if (isBlank(task.getName())) {
                        System.out.println("Skipped addTask because the task name was missing.");
                        break;
                    }
                    taskTools.addTask(task.getDateTime(), task.getName());
                    break;
                case "updateTask":
                    if (isBlank(task.getOldName()) || isBlank(task.getName())) {
                        System.out.println("Skipped updateTask because the task names were incomplete.");
                        break;
                    }
                    taskTools.updateTask(task.getOldName(), task.getName(), task.getDateTime());
                    break;
                case "deleteTask":
                    if (isBlank(task.getName())) {
                        System.out.println("Skipped deleteTask because the task name was missing.");
                        break;
                    }
                    taskTools.deleteTask(task.getName());
                    break;
                case "getTasks":
                    List<String> tasks = taskTools.getTasksToString();
                    tasks.forEach(System.out::println);
                    break;
                default:
                    System.out.println("Unknown task action: " + methodName);
            }
        }
    }

    private String callTool(String userMessage) {
        LocalDate today = LocalDate.now();
        String instructions = today + PromptProvider.getPrompt("callToolPrompt.txt");
        return AIService.SendRequestAndGetAnswer(hfUrl, hfToken, model, instructions + userMessage);
    }

    private void clearTaskList() {
        if (tasks != null) {
            tasks.clear();
        }
    }

    private void addTasksToCalendar(String jsonTasksToAdd) {
        if (jsonTasksToAdd == null || jsonTasksToAdd.isBlank()) {
            System.out.println("No tasks were selected to add.");
            return;
        }

        JSONObject json;
        try {
            json = new JSONObject(jsonTasksToAdd);
        } catch (Exception e) {
            System.out.println("The selected task list was not valid JSON.");
            return;
        }

        JSONArray tasksArray = json.optJSONArray("tasksToAdd");
        if (tasksArray == null || tasksArray.isEmpty()) {
            System.out.println("No tasks were selected to add.");
            return;
        }

        for (int i = 0; i < tasksArray.length(); i++) {
            JSONObject taskObj = tasksArray.optJSONObject(i);
            if (taskObj == null) {
                continue;
            }

            LocalDateTime deadline = parseDateTime(taskObj.optString("dateTime", null));
            JSONArray tasksTODO = taskObj.optJSONArray("taskNames");
            if (tasksTODO == null || tasksTODO.isEmpty()) {
                System.out.println("Skipped a selected task because no task names were provided.");
                continue;
            }

            String taskTitle = tasksTODO.toList().stream()
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.joining(", "));

            if (taskTitle.isEmpty()) {
                System.out.println("Skipped a selected task because the task name was empty.");
                continue;
            }

            taskTools.addTask(deadline, taskTitle);
        }
    }

    private String askUserWhichTasksAdd() {
        if (tasks == null || tasks.isEmpty()) {
            return "";
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("------------------------------------------------------------------------------------------------------");
        String system = "There are some tasks detected in your email. Do u want to add them to your to-do-list? If so then which one?";
        System.out.println(system);
        System.out.print("You: ");
        String userAnswer = scanner.nextLine().trim();
        String prompt = "\n" + "System: " + system + "\n" + "User: " + userAnswer + "\n";
        return getTasksToDo(prompt);
    }

    public void manageTaskList() {
        if (!hasPendingTasks()) {
            return;
        }
        String taskListToAdd = askUserWhichTasksAdd();
        System.out.println(taskListToAdd);
        addTasksToCalendar(taskListToAdd);
    }

    private String getTasksToDo(String userInput) {
        List<Task> safeTasks = tasks == null ? Collections.emptyList() : tasks;
        String instructions = PromptProvider.getPrompt("filteringTaskListPrompt.txt")
                + userInput
                + safeTasks.stream()
                .map(Task::getName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.joining("\n"));
        String responseAI = AIService.SendRequestAndGetAnswer(hfUrl, hfToken, model, instructions);
        clearTaskList();
        return responseAI;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("null")) {
            return null;
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            System.out.println("Skipped invalid date value: " + value);
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
