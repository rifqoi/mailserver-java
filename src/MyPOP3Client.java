import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

class MailMessage {
    private String message;
    private String recipient;
    private String sender;

    public MailMessage(String message, String recipient, String sender) {
        this.message = message;
        this.recipient = recipient;
        this.sender = sender;
    }

    public void printMail() {
        System.out.println("From\t\t: " + this.sender);
        System.out.println("Recipient\t: " + this.recipient);
        System.out.println("Content\t\t: " + this.message);
    }

}

class POP3Client {
    private final String host;
    private final String username;
    private final String password;

    public POP3Client(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public ArrayList<MailMessage> getMessages(int totalMessages) {
        Properties props = new Properties();
        props.put("mail.store.protocol", "pop3");
        props.put("mail.pop3.host", this.host);
        props.put("mail.pop3.port", "995");
        props.put("mail.pop3.ssl.enable", "true");

        Store emailStore = null;
        Folder emailFolder = null;
        try {
            Session emailSession = Session.getDefaultInstance(props);

            emailStore = emailSession.getStore();
            emailStore.connect(this.username, this.password);

            emailFolder = emailStore.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);
            ArrayList<MailMessage> mails = getMails(emailFolder, totalMessages);

            return mails;

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (emailFolder != null && emailFolder.isOpen()) {
                    emailFolder.close(false);
                }
                if (emailStore != null && emailStore.isConnected()) {
                    emailStore.close();
                }
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ArrayList<MailMessage> getMails(Folder emailFolder, int totalMessages)
            throws MessagingException, IOException {
        ArrayList<MailMessage> mails = new ArrayList<MailMessage>();
        for (Message message : emailFolder.getMessages(1, totalMessages)) {
            System.out.println(message);

            String recipient = message.getAllRecipients()[0].toString();
            String messageContent = message.getContent().toString();
            String sender = message.getFrom()[0].toString();
            MailMessage mail = new MailMessage(messageContent, recipient, sender);
            mails.add(mail);

        }

        return mails;

    }

}

public class MyPOP3Client {
    public static void main(String[] args) {
        String host = "pop.gmail.com";
        
        // Put your email and password here
        String email = "";
        String password = "";
        POP3Client pop3 = new POP3Client(host, email, password);
        List<MailMessage> messages = pop3.getMessages(10);
        messages.forEach((n) -> n.printMail());

    }
}
