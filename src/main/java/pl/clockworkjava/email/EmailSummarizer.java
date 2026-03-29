package pl.clockworkjava.email;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.clockworkjava.PromptProvider;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EmailSummarizer {

    private final String HF_URL = "https://router.huggingface.co/v1/chat/completions";
    private  final String HF_TOKEN = System.getenv("HF_TOKEN");
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    EmailClient emailClient = new EmailClient();

    public EmailResponse respond(String userMessage){
        List<Email> emails = new ArrayList<>();
        try {
            emails = emailClient.fetchUnreadMails();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getAIResponse(emails);
    }

    private EmailResponse getAIResponse(List<Email> emails){
        StringBuilder stringBuilder = new StringBuilder();
        EmailResponse response = new EmailResponse();
        try {
            String message;
            for (Email mail : emails) {
                message = PromptProvider.getPrompt("summarizationPrompt.txt") +
                        "Message from: " + mail.sender + " with the subject " + mail.subject + " " + mail.sender + " writes: " + mail.body;
                String mailTask = generateSummarization(message);
                response.tasks.add(mailTask);
                stringBuilder.append(mailTask);
            }
            stringBuilder.insert(0, PromptProvider.getPrompt("finalSummarizationPrompt.txt") + stringBuilder);
        } catch (Exception e){
            e.printStackTrace();
        }
        response.response = generateSummarization(stringBuilder.toString());
        return response;
    }

    private String generateSummarization(String prompt){
        try{
            JSONObject json = new JSONObject();
            JSONArray container = new JSONArray();
            JSONObject jsonMessages = new JSONObject();
            Response response;
            String responseAI;
            //json.put("model", "deepseek-ai/DeepSeek-R1:novita");
            json.put("model", "meta-llama/Llama-3.1-8B-Instruct:novita");
            json.put("stream", false);
            jsonMessages.put("content", prompt);
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
            response = client.newCall(request).execute();
            responseAI = response.body().string();
            JSONObject finalAnswer = new JSONObject(responseAI);
            JSONArray jsonArr = finalAnswer.getJSONArray("choices");
            prompt = jsonArr.getJSONObject(0).getJSONObject("message").getString("content");
            System.out.println(prompt);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return prompt;
    }
}
