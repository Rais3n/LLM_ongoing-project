package pl.clockworkjava.service;

import org.json.JSONArray;
import org.json.JSONObject;
import pl.clockworkjava.PromptProvider;
import pl.clockworkjava.aiTools.TaskTools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class TaskService {

    private List<String> tasks;

    private TaskTools taskTools = new TaskTools();
    private final String HF_URL = "https://router.huggingface.co/v1/chat/completions";
    private  final String HF_TOKEN = System.getenv("HF_TOKEN");
    private final String model = "meta-llama/Llama-3.1-8B-Instruct:novita";

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void respond(String userMessage){
        String tool = callTool(userMessage);
        System.out.println(tool);

        JSONObject json = new JSONObject(tool);
        int amountOfActions = json.getJSONArray("tools").length();
        for (int i = 0;i<amountOfActions;i++){
            JSONObject actionJSON = json.getJSONArray("tools").getJSONObject(i);
            String methodName = actionJSON.getString("name");
            String taskName;
            String oldTaskName = null;
            JSONObject parametersJSON = actionJSON.getJSONObject("arguments");
            LocalDateTime dateTime;
            if(!parametersJSON.isNull("date"))
                dateTime = LocalDateTime.parse(parametersJSON.getString("date"));
            else
                dateTime = null;
            if(parametersJSON.has("taskName"))
                taskName = parametersJSON.getString("taskName");
            else{
                JSONArray tasks = parametersJSON.getJSONArray("taskNames");
                taskName =  tasks.getString(1);
                oldTaskName = tasks.getString(0);
            }
            switch (methodName){
                case "addTask":
                    taskTools.addTask(dateTime,taskName);
                    break;
                case "updateTask":
                    taskTools.updateTask(oldTaskName, taskName, dateTime);
                    break;
                case "deleteTask":
                    taskTools.deleteTask(taskName);
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
        String instructions = "Today is " + today+ "\n"+
                "You are servant whose goal is to decide, based " +
                "on user message which method should be called."+
                "Possible methods to call:" + "\n" +
                "addTask(LocalDateTime dateTime, String task)," + "\n" +
                "updateTask(String taskName0,  String taskName1, LocalDateTime dateTime)," + "\n" +
                "deleteTask(LocalDateTime dateTime, String task)," + "\n" +
                "getTasks()" + "\n" +
                "In method updateTask, jsonArray taskNames always has to contain 2 objects"+
                "\n" + "Response in the given JSON format: "+ "\n" +
                "{\n" +
                "  \"tools\": [\n" +
                "    {\n" +
                "      \"name\": \"addTask\",\n" +
                "      \"arguments\": {\n" +
                "        \"taskName\": \"clean room\",\n" +
                "        \"date\": \"2026-03-30T10:00\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"updateTask\",\n" +
                "      \"arguments\": {\n" +
                "        \"taskNames\": [\"vacuum the floor\", \"cook dinner\"],\n" +
                "        \"date\": null\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}" + "\n" +
                "If no methods fit, return: { \"Tools\": [] }\n"+
                "Date in JSON has to be in format yyyy-mm-ddThh-mm. However if user did not provide exact time I want date to be null.\n"+
                "DO NOT add any extra text or explanation.\n" +
                "Now u will get user input: \n" ;
        return AIService.SendRequestAndGetAnswer(HF_URL,HF_TOKEN,model,instructions + userMessage);
    }

    private void clearTaskList(){
        tasks.clear();
    }

    private void addTasksFromString(String jsonTasksToAdd){
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
        tasks.stream().forEach(System.out::println);
        String system = "There are some tasks detected in your email. Do u want to add them to your to-do-list? If so then which one?";
        System.out.println(system);
        System.out.print("You: ");
        String userAnswer = scanner.nextLine().trim();
        String prompt =  "\n" + "System: " + system + "\n" + "User: " + userAnswer + "\n";
        return  getTasksToDo(prompt);
    }

    public void manageTaskList(){
        String taskListToAdd = askUserWhichTasksAdd();
        addTasksFromString(taskListToAdd);
    }

    private String getTasksToDo(String userInput){
        String instructions = PromptProvider.getPrompt("selectToolPrompt.txt") + userInput + String.join("\n",tasks);
        String responseAI;
        responseAI = AIService.SendRequestAndGetAnswer(HF_URL, HF_TOKEN, model,instructions);
        clearTaskList();
        return responseAI;
    }

}
