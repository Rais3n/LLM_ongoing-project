package pl.clockworkjava.orchestration;


import java.util.*;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class AgentRouter {

    private Map<String, float[]> embeddings = new HashMap<>();
    private final String HF_URL = "https://router.huggingface.co/scaleway/v1/embeddings";
    ;
    private final String HF_TOKEN = System.getenv("HF_TOKEN");
    private final OkHttpClient client = new OkHttpClient();

    AgentRouter() {
        Map<String, List<String>> categoryExamples = Map.of(
                "Task Manager", List.of("Add task", "Complete task", "Update task progress"),
                "Email Summarizer", List.of("Summarize email", "Extract key points from message", "Find action items"),
                "Meeting Scheduler", List.of("Schedule a meeting", "Check availability", "Set appointment")
        );

    }

    public String route(String userMessage) {
        String agent = null;
        float[] userEmbedding = getEmbeddings(userMessage);

        List<Map.Entry<String,Double>> topEntries = embeddings.entrySet()
                .stream()
                .map(entry -> (Map.Entry<String, Double>) new AbstractMap.SimpleEntry<>(
                        entry.getKey(),
                        cosineSimilarity(userEmbedding, entry.getValue())
                ))
                .sorted(Map.Entry.<String,Double>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .toList();
        agent = topEntries.getFirst().getKey();

        topEntries.stream().forEach(System.out::println);

        return agent;
    }

    private double[] toDoubleArray(float[] vector){
        double[] result = new double[vector.length];
        for(int i =0;i< vector.length;i++){
            result[i] = vector[i];
        }
        return result;
    }

    private double cosineSimilarity(float[] vec1, float[] vec2){
          RealVector v1 = new ArrayRealVector(toDoubleArray(vec1));
          RealVector v2 = new ArrayRealVector(toDoubleArray(vec2));
          double dotProduct = v1.dotProduct(v2);
          double normProduct = v1.getNorm()*v2.getNorm();

          return dotProduct / normProduct;

    }

    private float[] getEmbeddings(String label) {
        try {
            JSONObject json = new JSONObject();
            json.put("input", label);
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
}