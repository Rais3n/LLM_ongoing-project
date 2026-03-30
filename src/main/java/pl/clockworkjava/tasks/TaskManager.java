package pl.clockworkjava.tasks;

import org.json.JSONArray;
import org.json.JSONObject;
import pl.clockworkjava.AIService;
import pl.clockworkjava.PromptProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class TaskManager {

    private List<String> tasks;

    private TasksBackend tasksBackend = new TasksBackend();
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
    }

    private String callTool(String userMessage){
        LocalDate today = LocalDate.now();
        String instructions = "You are servant whose goal is to decide, based " +
                "on user message which method should be called."+
                "Possible methods to call:" + "\n" +
                "addTask(LocalDateTime dateTime, String task)," + "\n" +
                "updateTask(String oldTaskName,  String newTaskName, LocalDateTime dateTime)," + "\n" +
                "deleteTask(LocalDateTime dateTime, String task)," + "\n" +
                "getTasks()" + "\n" +
                "These methods are responsible for logic in Java application."+
                "\n" + "Response in the given JSON format: "+ "\n" +
                "{" + "\n" +
                    "\"Tools\": [" + "\n" +
                    "{" +"\n"+
                        "\"methodName\": \"addTask\" ," + "\n" +
                        "\"taskName\": \"clean room\"," + "\n" +
                        "\"date\": \"2026-03-30T10:00\"" + "\n" +
                    "}," + "\n"+
                    "{" +"\n"+
                    "\"methodName\": \"updateTask\" ," + "\n" +
                    "\"taskName\": [\"vacuum the floor\", \"cook dinner\"]," + "\n" +
                    "\"date\": \"\"" + "\n" +
                    "}]" + "\n" +
                "}" + "\n" +
                "If no methods fit, return: { \"Tools\": [] }"+
                "DO NOT add any extra text or explanation.\n" +
                "Today is " + today+ "\n"+
                "Now u will get user input: \n" ;
        String response = AIService.SendRequestAndGetAnswer(HF_URL,HF_TOKEN,model,userMessage);
        System.out.println(response);
        return "TODO";
    }

    private void clearTaskList(){
        tasks.clear();
    }

    private void scanTheTaskListAndAddTasks(String jsonTasksToAdd){
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
            tasksBackend.addTask(deadline, sb.toString());

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
        return  getCurrentTaskList(prompt);
    }

    public void manageTaskList(){
        String taskListToAdd = askUserWhichTasksAdd();
        scanTheTaskListAndAddTasks(taskListToAdd);
    }

    private String getCurrentTaskList(String userInput){
        String instructions = PromptProvider.getPrompt("selectToolPrompt.txt") + userInput + String.join("\n",tasks);
        String responseAI;
        responseAI = AIService.SendRequestAndGetAnswer(HF_URL, HF_TOKEN, model,instructions);
        clearTaskList();
        return responseAI;
    }

}
