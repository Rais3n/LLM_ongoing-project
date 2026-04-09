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
        String agent ="";
        try {
            agent = agentRouter.route(userMessage);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        String finalResponse = new String();

        switch (agent){
            case "Task Manager":
                taskService.respond(userMessage);
                finalResponse = "Work is done!";
                break;
            case "Email Summarizer":
                emailResponseDTO =  emailService.respond();
                System.out.println(emailResponseDTO.response);
                taskService.setTasks(emailResponseDTO.getTasks());
                if(!taskService.getTasks().isEmpty())
                    taskService.manageTaskList();
                break;
            default:
                break;
        }

        return finalResponse;
    }
}
