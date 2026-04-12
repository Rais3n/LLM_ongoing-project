package pl.clockworkjava.model;

public class Email {

    private String subject;
    private String sender;
    private String body;

    public Email() {
    }

    public Email(String subject, String sender, String body) {
        this.subject = subject;
        this.sender = sender;
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
