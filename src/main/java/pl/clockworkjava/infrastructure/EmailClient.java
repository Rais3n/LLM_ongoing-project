package pl.clockworkjava.infrastructure;

import org.jsoup.Jsoup;
import pl.clockworkjava.AppConfig;
import pl.clockworkjava.model.Email;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailClient {

    private static final String IMAP_HOST = "imap.gmail.com";
    private static final int MAX_RECENT_EMAILS = 10;

    private final String password = AppConfig.getEmailPassword();
    private final String emailUsername = AppConfig.getEmailUsername();

    private Store store;
    private Folder inbox;

    public void connect() throws Exception {
        if (emailUsername == null || emailUsername.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException("Missing email credentials. Set EMAIL_USERNAME and GMAIL_APP_PASSWORD.");
        }

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props, null);
        store = session.getStore("imaps");
        store.connect(IMAP_HOST, emailUsername, password);

        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
    }

    public void disconnect() throws Exception {
        if (inbox != null && inbox.isOpen()) {
            inbox.close(false);
        }
        if (store != null && store.isConnected()) {
            store.close();
        }
    }

    public List<Email> fetchUnreadMails() throws Exception {
        List<Email> emails = new ArrayList<>();

        try {
            connect();
            int total = inbox.getMessageCount();
            if (total == 0) {
                return emails;
            }

            int start = Math.max(1, total - MAX_RECENT_EMAILS + 1);
            Message[] recentMessages = inbox.getMessages(start, total);

            for (Message msg : recentMessages) {
                if (!msg.isSet(Flags.Flag.SEEN)) {
                    emails.add(mapToEmail(msg));
                }
            }
        } finally {
            disconnect();
        }

        return emails;
    }

    private Email mapToEmail(Message msg) throws Exception {
        Address[] fromAddresses = msg.getFrom();
        String sender = fromAddresses != null && fromAddresses.length > 0
                ? fromAddresses[0].toString()
                : "Unknown sender";
        String subject = msg.getSubject() == null ? "(no subject)" : msg.getSubject();
        String body = getTextFromMessage(msg);

        return new Email(subject, sender, body);
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return cleanText((String) message.getContent());
        } else if (message.isMimeType("multipart/*")) {
            return getTextFromMimeMultipart((MimeMultipart) message.getContent());
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(cleanText((String) bodyPart.getContent()));
            } else if (bodyPart.isMimeType("text/html")) {
                result.append(cleanText((String) bodyPart.getContent()));
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private String cleanText(String htmlText) {
        return Jsoup.parse(htmlText)
                .text()
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
