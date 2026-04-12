package pl.clockworkjava.service;

import pl.clockworkjava.AppConfig;
import pl.clockworkjava.PromptProvider;
import pl.clockworkjava.dto.EmailResponseDTO;
import pl.clockworkjava.infrastructure.EmailClient;
import pl.clockworkjava.model.Email;
import pl.clockworkjava.model.Task;

import java.util.ArrayList;
import java.util.List;

public class EmailService {

    private static final String NO_EMAILS_MESSAGE = "I didn't find any unread emails to summarize.";

    private final String hfUrl = AppConfig.CHAT_COMPLETIONS_URL;
    private final String hfToken = AppConfig.getHfToken();
    private final String llamaModel = AppConfig.DEFAULT_CHAT_MODEL;
    private final EmailClient emailClient = new EmailClient();

    public EmailResponseDTO respond() {
        List<Email> emails = new ArrayList<>();
        try {
            emails = emailClient.fetchUnreadMails();
        } catch (Exception e) {
            e.printStackTrace();
            EmailResponseDTO response = new EmailResponseDTO();
            response.setResponse("I couldn't read your inbox right now.");
            return response;
        }

        if (emails.isEmpty()) {
            EmailResponseDTO response = new EmailResponseDTO();
            response.setResponse(NO_EMAILS_MESSAGE);
            return response;
        }

        return getAIResponse(emails);
    }

    private EmailResponseDTO getAIResponse(List<Email> emails) {
        List<String> summaries = new ArrayList<>();
        EmailResponseDTO response = new EmailResponseDTO();

        try {
            for (Email mail : emails) {
                String message = PromptProvider.getPrompt("summarizationPrompt.txt")
                        + "Message from: " + mail.getSender()
                        + " with the subject " + mail.getSubject()
                        + ". " + mail.getSender()
                        + " writes: " + mail.getBody();

                String mailTask = AIService.SendRequestAndGetAnswer(hfUrl, hfToken, llamaModel, message);
                if (mailTask == null || mailTask.isBlank()) {
                    continue;
                }

                summaries.add(mailTask);
                response.addTask(new Task(mailTask));
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setResponse("I couldn't summarize your unread emails.");
            return response;
        }

        if (summaries.isEmpty()) {
            response.setResponse("I found unread emails, but I couldn't extract a useful summary.");
            return response;
        }

        String combinedSummaryPrompt = PromptProvider.getPrompt("finalSummarizationPrompt.txt")
                + String.join(System.lineSeparator(), summaries);
        response.setResponse(AIService.SendRequestAndGetAnswer(hfUrl, hfToken, llamaModel, combinedSummaryPrompt));
        return response;
    }
}
