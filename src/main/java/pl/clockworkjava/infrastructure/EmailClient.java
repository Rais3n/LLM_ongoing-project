package pl.clockworkjava.infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.*;
import java.util.*;
import javax.mail.internet.MimeMultipart;
import org.jsoup.Jsoup;
import pl.clockworkjava.model.Email;

public class EmailClient {
    private Store store;
    private Folder inbox;
    private final String PASSWORD = System.getenv("GMAIL_APP_PASSWORD");


    public void connect() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props, null);
        store = session.getStore("imaps");
        store.connect("imap.gmail.com", "p.dziedzic52@gmail.com", PASSWORD);

        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
    }

    public void disconnect() throws Exception {
        if (inbox != null && inbox.isOpen()) inbox.close(false);
        if (store != null && store.isConnected()) store.close();
    }


    public List<Email> fetchUnreadMails() throws Exception{
        List<Email> emails = new ArrayList<>();

        connect();
        int total = inbox.getMessageCount();
        int end = total;
        int numOfMails = 10;
        int start = Math.max(1, total - numOfMails + 1);
        Message[] last10 = inbox.getMessages(start, end);

        for (Message msg : last10){
            if (!msg.isSet(Flags.Flag.SEEN)){
                emails.add(mapToEmail(msg));
            }
        }

        disconnect();

        return emails;

    }

    private Email mapToEmail(Message msg) throws Exception {
        Email email = new Email();

        email.sender = msg.getFrom()[0].toString();
        email.subject = msg.getSubject();
        email.body = getTextFromMessage(msg);

        return email;
    }
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            String html = (String) message.getContent();

            return cleanText(html);
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                String text = (String) bodyPart.getContent();
                result.append(cleanText(text));
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append(cleanText(html));
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private String cleanText(String htmlText){
        htmlText = Jsoup.parse(htmlText)
                .text()                       // plain text
                .replace("\u00A0", " ")       // normalize spaces
                .replaceAll("\\s+", " ")      // collapse multiple spaces/newlines
                .trim();
        return htmlText;
    }
}
