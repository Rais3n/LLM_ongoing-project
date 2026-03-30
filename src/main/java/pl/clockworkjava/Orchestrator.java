package pl.clockworkjava;

import pl.clockworkjava.email.EmailResponse;
import pl.clockworkjava.email.EmailSummarizer;
import pl.clockworkjava.tasks.TaskManager;

import java.util.List;

public class Orchestrator {


    TaskManager taskManager = new TaskManager();
    MeetingScheduler meetingScheduler = new MeetingScheduler();
    EmailSummarizer emailSummarizer = new EmailSummarizer();

    private final AgentRouter agentRouter = new AgentRouter();

    public String handleMessage(String userMessage){

        EmailResponse emailResponse;
        String agent;// = agentRouter.route(userMessage);
        String finalResponse;

        agent = "Email Summarizer"; //DEBUGOWANIE
        switch (agent){
            case "Task Manager":
                taskManager.respond(userMessage);
                break;
            case "Meeting Scheduler":
                meetingScheduler.respond(userMessage);
                break;
            case "Email Summarizer":
                emailResponse =  emailSummarizer.respond();
                taskManager.setTasks(emailResponse.tasks);
                if(!taskManager.getTasks().isEmpty())
                    taskManager.manageTaskList();
                break;
            default:
                break;
        }


        return agent;
    }
}
