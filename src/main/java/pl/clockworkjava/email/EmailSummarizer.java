package pl.clockworkjava.email;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

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

    public void respond(String userMessage){
        try {
            List<Email> emails = emailClient.fetchUnreadMails();
            askAI(emails);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EmailResponse askAI(List<Email> emails){
        StringBuilder stringBuilder = new StringBuilder();
        EmailResponse response = new EmailResponse();
        try {
            String message;
            for (Email mail : emails) {
                message = "You are an assistant that summarizes emails for busy users.\n" +
                        "\n" +
                        "Task:\n" +
                        "Extract and summarize only the most important information from the email below.\n" +
                        "\n" +
                        "Instructions:\n" +
                        "- Identify the main purpose of the email\n" +
                        "- Extract key points\n" +
                        "- Highlight any required actions or deadlines\n" +
                        "- Ignore greetings, signatures, and irrelevant details\n" +
                        "\n" +
                        "Output format:\n" +
                        "{\n" +
                        "  \"main_topic\": \"...\",\n" +
                        "  \"key_points\": [\"...\", \"...\"],\n" +
                        "  \"action_items\": [\"...\", \"...\"]\n" +
                        "}\n" +
                        "\n" +
                        "Email:\n" +
                        "\"\"\"\n" +
                        "{email_content}\n" +
                        "\"\"\"\n" +
                        "Message from: " + mail.sender + " with the subject " + mail.subject + " " + mail.sender + " writes: " + mail.body;
                response.tasks.add(message);
                stringBuilder.append(generateSummarization(message));
            }
            stringBuilder.insert(0, "You are an expert assistant who summarizes information clearly and concisely. \n" +
                    "I will give you a list of email summaries. Each summary is placed in JSON object. \n" +
                    "\n" +
                    "Your task is to read all the summaries and create a single, cohesive summary that highlights the most important points, key actions, and takeaways. Focus on clarity and brevity. \n" +
                    "\n" +
                    "Do not add information not present in the summaries.\n" +
                    "\n" +
                    "Here are the summaries: " + stringBuilder);

        } catch (Exception e){
            e.printStackTrace();
        }
        response.respone  = generateSummarization(stringBuilder.toString());
        return response;
        //return generateSummarization(stringBuilder.toString());
    }

    private String generateSummarization(String text){
        try{
            JSONObject json = new JSONObject();
            JSONArray container = new JSONArray();
            JSONObject jsonMessages = new JSONObject();
            Response response;
            String responseAI;
            json.put("model", "deepseek-ai/DeepSeek-R1:novita");
            json.put("stream", false);
            jsonMessages.put("content", text);
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
            text = jsonArr.getJSONObject(0).getJSONObject("message").getString("content");
            System.out.println(text);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return text;
    }
}
