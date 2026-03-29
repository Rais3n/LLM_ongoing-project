package pl.clockworkjava;


import java.nio.file.Files;
import java.nio.file.Paths;

public class PromptProvider {

    private static final String PROMPTS_DIR = "prompts";

    public static String getPrompt(String fileName){
        String path = Paths.get(PROMPTS_DIR, fileName).toString();
        String prompt = "";
        try{
            prompt = new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e){
            System.out.println("Failed to load prompt: " + e.getMessage());
        }
        return prompt;
    }
}