package pl.clockworkjava.tasks;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.clockworkjava.PromptProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TaskManager {
    private List<String> tasks;

    private TasksBackend tasksBackend = new TasksBackend();
    private final String HF_URL = "https://router.huggingface.co/v1/chat/completions";
    private  final String HF_TOKEN = System.getenv("HF_TOKEN");
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();


    public void respond(String userMessage){

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

    private String askUserWhichTasksAdd(List<String> tasks){
        Scanner scanner =  new Scanner(System.in);
        tasks.stream().forEach(System.out::println);
        String system = "There are some tasks detected in your email. Do u want to add them to your to-do-list? If so then which one?";
        System.out.println(system);
        System.out.print("You: ");
        String userAnswer = scanner.nextLine().trim();
        String prompt =  "\n" + "System: " + system + "\n" + "User: " + userAnswer + "\n";
        return  selectTool(tasks, prompt);
    }

    public void manageTaskList(List<String> tasks){
        String taskListToAdd = askUserWhichTasksAdd(tasks);
        scanTheTaskListAndAddTasks(taskListToAdd);
    }

    private String selectTool(List<String> tasks, String userInput){
        String instructions = PromptProvider.getPrompt("selectToolPrompt.txt") + userInput + String.join("\n",tasks);

        JSONObject json = new JSONObject();
        JSONArray container = new JSONArray();
        JSONObject jsonMessages = new JSONObject();
        Response response;
        String responseAI = null;
        //json.put("model", "deepseek-ai/DeepSeek-R1:novita");
        json.put("model", "Qwen/Qwen3-Coder-Next:novita");
        json.put("stream", false);
        jsonMessages.put("content", instructions);
        jsonMessages.put("role", "user");
        container.put(jsonMessages);
        json.put("messages", container);
        RequestBody requestBody = RequestBody.create(json.toString(),
                MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(HF_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + HF_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            response = client.newCall(request).execute();
            responseAI = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject finalAnswer = new JSONObject(responseAI);
        JSONArray jsonArr = finalAnswer.getJSONArray("choices");
        responseAI = jsonArr.getJSONObject(0).getJSONObject("message").getString("content");

        return responseAI;
    }

}
