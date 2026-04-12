package pl.clockworkjava;


import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PromptProvider {

    private static final String PROMPTS_DIR = "prompts";

    public static String getPrompt(String fileName) {
        Path path = Paths.get(PROMPTS_DIR, fileName);
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Failed to load prompt: " + e.getMessage());
            return "";
        }
    }
}
