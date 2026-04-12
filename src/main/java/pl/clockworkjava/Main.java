package pl.clockworkjava;


import pl.clockworkjava.orchestration.Orchestrator;

import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Orchestrator orchestrator = new Orchestrator();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("You: ");
                String userMessage = scanner.nextLine().trim();
                if (userMessage.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }
                if (userMessage.isBlank()) {
                    System.out.println("Assistant: Please enter a message.");
                    continue;
                }

                String response = orchestrator.handleMessage(userMessage);
                System.out.println("Assistant: " + response);
                if (orchestrator.hasPendingEmailTasks()) {
                    orchestrator.managePendingEmailTasks();
                }
            }
        }
    }
}
