package pl.clockworkjava.orchestration;

import pl.clockworkjava.dto.EmailResponseDTO;
import pl.clockworkjava.service.EmailService;
import pl.clockworkjava.service.TaskService;

public class Orchestrator {

    private final TaskService taskService = new TaskService();
    private final EmailService emailService = new EmailService();
    private final AgentRouter agentRouter = new AgentRouter();

    public String handleMessage(String userMessage) {
        String agent;
        try {
            agent = agentRouter.route(userMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return "I couldn't decide which agent should handle that request.";
        }

        switch (agent) {
            case "Task Manager":
                taskService.respond(userMessage);
                return "Task request processed.";
            case "Email Summarizer":
                EmailResponseDTO emailResponseDTO = emailService.respond();
                taskService.setTasks(emailResponseDTO.getTasks());
                return emailResponseDTO.getResponse();
            default:
                return "I couldn't match that request to an available agent.";
        }
    }

    public boolean hasPendingEmailTasks() {
        return taskService.hasPendingTasks();
    }

    public void managePendingEmailTasks() {
        taskService.manageTaskList();
    }
}
