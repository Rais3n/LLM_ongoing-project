package pl.clockworkjava;

import pl.clockworkjava.email.EmailSummarizer;

public class Orchestrator {


    TaskManager taskManager = new TaskManager();
    MeetingScheduler meetingScheduler = new MeetingScheduler();
    EmailSummarizer emailSummarizer = new EmailSummarizer();

    private AgentRouter agentRouter = new AgentRouter();

    public String handleMessage(String userMessage){

        String agent = agentRouter.route(userMessage);

        agent = "Email Summarizer"; //DEBUGOWANIE
        switch (agent){
            case "Task Manager":
                taskManager.respond(userMessage);
                break;
            case "Meeting Scheduler":
                meetingScheduler.respond(userMessage);
                break;
            case "Email Summarizer":
                emailSummarizer.respond(userMessage);
                break;
            default:
                break;
        }


        return agent;
    }
}
