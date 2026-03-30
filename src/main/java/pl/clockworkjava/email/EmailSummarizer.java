package pl.clockworkjava.email;

import pl.clockworkjava.AIService;
import pl.clockworkjava.PromptProvider;

import java.util.ArrayList;
import java.util.List;

public class EmailSummarizer {

    private final String HF_URL = "https://router.huggingface.co/v1/chat/completions";
    private  final String HF_TOKEN = System.getenv("HF_TOKEN");
    private final String LlamaModel = "meta-llama/Llama-3.1-8B-Instruct:novita";

    EmailClient emailClient = new EmailClient();

    public EmailResponse respond(){
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
                String mailTask = AIService.SendRequestAndGetAnswer(HF_URL,HF_TOKEN, LlamaModel,message);
                response.tasks.add(mailTask);
                stringBuilder.append(mailTask);
            }
            stringBuilder.insert(0, PromptProvider.getPrompt("finalSummarizationPrompt.txt") + stringBuilder);
        } catch (Exception e){
            e.printStackTrace();
        }
        response.response = AIService.SendRequestAndGetAnswer(HF_URL,HF_TOKEN, LlamaModel,stringBuilder.toString());
        return response;
    }
}
