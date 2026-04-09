package pl.clockworkjava.orchestration;


import java.util.*;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class AgentRouter {
    private final String HF_URL = "https://router.huggingface.co/scaleway/v1/embeddings";
    ;
    private final String HF_TOKEN = System.getenv("HF_TOKEN");
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    AgentRouter() {
        Map<String, List<String>> categoryExamples = Map.of(
                "Task Manager", List.of("Add task", "Complete task", "Update task progress"),
                "Email Summarizer", List.of("Summarize email", "Extract key points from message", "Find action items"),
                "Meeting Scheduler", List.of("Schedule a meeting", "Check availability", "Set appointment")
        );

        uploadCategoriesToQdrant(categoryExamples);

    }

    private float[] averageEmbeddings(List<float[]> embeddings){
        float[] resultVector = null;
        int dim =  0;
        int numOfVectors=1;
        if(!embeddings.isEmpty()) {
            dim = embeddings.get(0).length;
            resultVector = new float[dim];
            numOfVectors = embeddings.size();
        }
        for(float[] vector : embeddings)
            for(int i=0;i<dim;i++){
                resultVector[i] += vector[i]/numOfVectors;
            }
        return  resultVector;
    }


    private float[] getEmbeddings(String expressionToEmbedding) {
        try {
            JSONObject json = new JSONObject();
            json.put("input", expressionToEmbedding);
            json.put("model", "qwen3-embedding-8b");
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(HF_URL)
                    .header("Authorization", "Bearer " + HF_TOKEN)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();

            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray dataArray = jsonResponse.getJSONArray("data");
            JSONObject embeddingObject = dataArray.getJSONObject(0);
            JSONArray vectorArray = embeddingObject.getJSONArray("embedding");
            float[] embeddings = new float[vectorArray.length()];
            for (int i = 0; i < vectorArray.length(); i++) {
                embeddings[i] = (float) vectorArray.getDouble(i);
            }
            return embeddings;
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

    private void uploadCategoriesToQdrant(Map<String, List<String>> categoryExamples) {
        String collectionName = "categories";
        if (!isCollectionEmpty(collectionName)) {
            System.out.println("Collection already has embeddings. Skipping upload.");
            return;
        }

        System.out.println("Uploading category embeddings...");

        Map<String, float[]> embeddings = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : categoryExamples.entrySet()) {
            List<float[]> exampleEmbeddings = entry.getValue().stream()
                    .map(this::getEmbeddings)
                    .toList();
            embeddings.put(entry.getKey(), averageEmbeddings(exampleEmbeddings));
        }

        embeddings.forEach((category, vector) -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("category", category);
                payload.put("examples", new JSONArray(categoryExamples.get(category)));

                JSONObject point = new JSONObject();
                point.put("id", UUID.randomUUID().toString());
                point.put("vector", new JSONArray(vector));
                point.put("payload", payload);

                JSONObject bodyJson = new JSONObject();
                bodyJson.put("points", new JSONArray().put(point));

                RequestBody body = RequestBody.create(bodyJson.toString(),
                        MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url("http://localhost:6333/collections/categories/points")
                        .put(body)
                        .build();

                Response response = client.newCall(request).execute();
                System.out.println("Uploaded " + category + ": " + response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public String route(String userMessage) throws IOException {
        float[] userEmbedding = getEmbeddings(userMessage);

        JSONObject searchJson = new JSONObject();
        searchJson.put("vector", new JSONArray(userEmbedding));
        searchJson.put("limit", 1);
        searchJson.put("with_payload", true);

        RequestBody body = RequestBody.create(searchJson.toString(),
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("http://localhost:6333/collections/categories/points/search")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        JSONObject jsonResponse = new JSONObject(response.body().string());
        JSONArray result = jsonResponse.getJSONArray("result");
        if (result.length() > 0) {
            JSONObject first = result.getJSONObject(0);
            return first.getJSONObject("payload").getString("category");
        }
        return null;
    }


    private boolean isCollectionEmpty(String collectionName) {
        try {
            Request request = new Request.Builder()
                    .url("http://localhost:6333/collections/" + collectionName)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            JSONObject jsonResponse = new JSONObject(response.body().string());
            int count = jsonResponse.getJSONObject("result").getInt("points_count");
            return count == 0;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }
}