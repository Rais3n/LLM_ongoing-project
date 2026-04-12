package pl.clockworkjava.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class AIService {

    private static final String DEFAULT_ERROR_MESSAGE = "I couldn't process that request right now.";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    public static String SendRequestAndGetAnswer(String HF_URL, String HF_TOKEN, String model, String prompt) {
        if (HF_URL == null || HF_URL.isBlank() || HF_TOKEN == null || HF_TOKEN.isBlank()) {
            System.err.println("Missing AI service configuration.");
            return DEFAULT_ERROR_MESSAGE;
        }

        JSONObject json = new JSONObject();
        prepareJSONForRequest(json, model, prompt);
        Request request = createRequest(HF_URL, HF_TOKEN, json);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("AI request failed with status code: " + response.code());
                return DEFAULT_ERROR_MESSAGE;
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                System.err.println("AI request returned an empty response body.");
                return DEFAULT_ERROR_MESSAGE;
            }

            String responseAI = responseBody.string();
            if (responseAI == null || responseAI.isBlank()) {
                System.err.println("AI request returned an empty payload.");
                return DEFAULT_ERROR_MESSAGE;
            }

            JSONObject finalAnswer = new JSONObject(responseAI);
            return getResponseAIFromJSON(finalAnswer);
        } catch (Exception e) {
            e.printStackTrace();
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    private static String getResponseAIFromJSON(JSONObject finalAnswer) {
        JSONArray jsonArr = finalAnswer.optJSONArray("choices");
        if (jsonArr == null || jsonArr.isEmpty()) {
            return DEFAULT_ERROR_MESSAGE;
        }

        JSONObject firstChoice = jsonArr.optJSONObject(0);
        if (firstChoice == null) {
            return DEFAULT_ERROR_MESSAGE;
        }

        JSONObject message = firstChoice.optJSONObject("message");
        if (message == null) {
            return DEFAULT_ERROR_MESSAGE;
        }

        String content = message.optString("content", "").trim();
        return content.isEmpty() ? DEFAULT_ERROR_MESSAGE : content;
    }

    private static Request createRequest(String HF_URL, String HF_TOKEN, JSONObject json) {
        RequestBody requestBody = RequestBody.create(json.toString(),
                MediaType.parse("application/json"));

        return new Request.Builder()
                .url(HF_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + HF_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private static void prepareJSONForRequest(JSONObject json, String model, String prompt) {
        JSONArray containerForPrompt = new JSONArray();
        JSONObject jsonMessages = new JSONObject();
        json.put("model", model);
        json.put("stream", false);
        jsonMessages.put("content", prompt);
        jsonMessages.put("role", "user");
        containerForPrompt.put(jsonMessages);
        json.put("messages", containerForPrompt);
    }

}
