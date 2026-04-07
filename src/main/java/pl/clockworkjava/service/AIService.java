package pl.clockworkjava.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class AIService {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    public static String SendRequestAndGetAnswer(String HF_URL, String HF_TOKEN, String model, String prompt) {
        Response response;
        String responseAI = null;

        JSONObject json = new JSONObject();
        prepareJSONForRequest(json,model,prompt);
        Request request = createRequest(HF_URL,HF_TOKEN,json);
        try {
            response = client.newCall(request).execute();
            responseAI = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject finalAnswer = new JSONObject(responseAI);
        return getResponseAIFromJSON(finalAnswer);
    }

    private static String getResponseAIFromJSON(JSONObject finalAnswer){
        JSONArray jsonArr = finalAnswer.getJSONArray("choices");
        return jsonArr.getJSONObject(0).getJSONObject("message").getString("content");
    }

    private static Request createRequest(String HF_URL, String HF_TOKEN, JSONObject json){
        RequestBody requestBody = RequestBody.create(json.toString(),
                MediaType.parse("application/json"));

        return new Request.Builder()
                .url(HF_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + HF_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private static void prepareJSONForRequest(JSONObject json,String model,String prompt){
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
