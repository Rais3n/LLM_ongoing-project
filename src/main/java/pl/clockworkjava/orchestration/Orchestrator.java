package pl.clockworkjava.orchestration;

import pl.clockworkjava.dto.EmailResponseDTO;
import pl.clockworkjava.service.EmailService;
import pl.clockworkjava.service.TaskService;

public class Orchestrator {


    TaskService taskService = new TaskService();
    EmailService emailService = new EmailService();

    private final AgentRouter agentRouter = new AgentRouter();

    public String handleMessage(String userMessage){

        EmailResponseDTO emailResponseDTO;
        String agent;// = agentRouter.route(userMessage);
        String finalResponse;

        agent = "Email Summarizer"; //DEBUGOWANIE
        switch (agent){
            case "Task Manager":
                taskService.respond(userMessage);
                break;
            case "Email Summarizer":
                emailResponseDTO =  emailService.respond();
                taskService.setTasks(emailResponseDTO.tasks);
                if(!taskService.getTasks().isEmpty())
                    taskService.manageTaskList();
                break;
            default:
                break;
        }


        return agent;
    }
}
