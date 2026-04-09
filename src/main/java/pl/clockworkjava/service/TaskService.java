package pl.clockworkjava.service;

import org.json.JSONArray;
import org.json.JSONObject;
import pl.clockworkjava.PromptProvider;
import pl.clockworkjava.aiTools.TaskTools;
import pl.clockworkjava.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TaskService {

    private List<Task> tasks;

    private TaskTools taskTools = new TaskTools();
    private final String HF_URL = "https://router.huggingface.co/v1/chat/completions";
    private  final String HF_TOKEN = System.getenv("HF_TOKEN");
    private final String model = "meta-llama/Llama-3.1-8B-Instruct:novita";

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    private Task convertJsonToTask(JSONObject jsonTask){
        Task task = new Task();
        JSONObject parametersJSON = jsonTask.getJSONObject("arguments");
        String taskName;
        String oldTaskName;

        LocalDateTime dateTime;

        if(!parametersJSON.isNull("date")) {
            dateTime = LocalDateTime.parse(parametersJSON.getString("date"));
        }
        else {
            dateTime = null;
        }

        if(parametersJSON.has("taskName"))
            taskName = parametersJSON.getString("taskName");
        else{
            JSONArray tasks = parametersJSON.getJSONArray("taskNames");
            taskName =  tasks.getString(1);
            oldTaskName = tasks.getString(0);
            task.setOldName(oldTaskName);
        }

        task.setDateTime(dateTime);
        task.setName(taskName);

        return task;
    }

    public void respond(String userMessage){
        String tool = callTool(userMessage);
        System.out.println(tool);

        JSONObject json = new JSONObject(tool);

        int numberOfTasks = json.getJSONArray("tools").length();
        for (int i = 0;i<numberOfTasks;i++){
            JSONObject actionJSON = json.getJSONArray("tools").getJSONObject(i);
            String methodName = actionJSON.getString("name");

            Task task = convertJsonToTask(actionJSON);

            switch (methodName){
                case "addTask":
                    taskTools.addTask(task.getDateTime(),task.getName());
                    break;
                case "updateTask":
                    taskTools.updateTask(task.getOldName(), task.getName(), task.getDateTime());
                    break;
                case "deleteTask":
                    taskTools.deleteTask(task.getName());
                    break;
                case "getTasks":
                    List<String> tasks = taskTools.getTasksToString();
                    tasks.stream().forEach(System.out::println);
                    break;
                default:
                    System.out.println("NOTHING");

            }
        }


    }

    private String callTool(String userMessage){
        LocalDate today = LocalDate.now();
        String instructions = today + PromptProvider.getPrompt("callToolPrompt.txt");
        return AIService.SendRequestAndGetAnswer(HF_URL,HF_TOKEN,model,instructions + userMessage);
    }

    private void clearTaskList(){
        tasks.clear();
    }

    private void addTasksToCalendar(String jsonTasksToAdd){
        JSONObject json = new JSONObject(jsonTasksToAdd);
        JSONArray tasksArray = json.getJSONArray("tasksToAdd");

        for (int i =0;i<tasksArray.length();i++){

            LocalDateTime deadline  = null;
            JSONObject taskObj = tasksArray.getJSONObject(i);
            String dateTimeStr = taskObj.optString("dateTime", null);

            if (dateTimeStr != null && !dateTimeStr.equals("null")) {
                deadline = LocalDateTime.parse(dateTimeStr);
            }

            JSONArray tasksTODO = (JSONArray)((JSONObject)tasksArray.get(i)).get("taskNames");
            StringBuilder sb = new StringBuilder();
            for(int j = 0;j<tasksTODO.length();j++){
                sb.append(tasksTODO.get(j)+ ", ");
            }
            taskTools.addTask(deadline, sb.toString());

        }
    }

    private String askUserWhichTasksAdd(){
        Scanner scanner =  new Scanner(System.in);
        System.out.println("------------------------------------------------------------------------------------------------------");
        tasks.stream().map(Task::getName).forEach(System.out::println);
        String system = "There are some tasks detected in your email. Do u want to add them to your to-do-list? If so then which one?";
        System.out.println(system);
        System.out.print("You: ");
        String userAnswer = scanner.nextLine().trim();
        String prompt =  "\n" + "System: " + system + "\n" + "User: " + userAnswer + "\n";
        return  getTasksToDo(prompt);
    }

    public void manageTaskList(){
        String taskListToAdd = askUserWhichTasksAdd();
        System.out.println(taskListToAdd);
        addTasksToCalendar(taskListToAdd);
    }

    private String getTasksToDo(String userInput){
        String instructions = PromptProvider.getPrompt("filteringTaskListPrompt.txt") + userInput + tasks.stream().map(Task::getName).collect(Collectors.joining("\n"));
        String responseAI;
        responseAI = AIService.SendRequestAndGetAnswer(HF_URL, HF_TOKEN, model,instructions);
        clearTaskList();
        return responseAI;
    }

}
