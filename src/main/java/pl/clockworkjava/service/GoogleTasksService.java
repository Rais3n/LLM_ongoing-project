package pl.clockworkjava.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;

import java.io.FileReader;
import java.util.Collections;

public class GoogleTasksService {
    private static final String APPLICATION_NAME = "Java Calendar App";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static Tasks getService() throws Exception {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY, new FileReader("credentials.json"));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                Collections.singleton(TasksScopes.TASKS))
                .setAccessType("offline")
                .build();

        AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver());
        var credential = app.authorize("user");

        return new Tasks.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}