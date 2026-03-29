package pl.clockworkjava;

import java.io.IOException;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner =  new Scanner(System.in);

        Orchestrator orchestrator = new Orchestrator();
        while(true){
            System.out.print("You: ");
            String userMessage = scanner.nextLine().trim();
            if (userMessage.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }
            String response = orchestrator.handleMessage(userMessage);

            //System.out.println("Assistant: " + response);
        }
    }
}