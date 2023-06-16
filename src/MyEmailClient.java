import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

class MailMessage {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String recipient;

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    private String sender;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    private String subject;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public MailMessage(String message, String recipient, String sender, String subject) {
        this.message = message;
        this.recipient = recipient;
        this.sender = sender;
        this.subject = subject;
    }

    public void printMail() {
        System.out.println("From\t\t: " + this.sender);
        System.out.println("Recipient\t: " + this.recipient);
        System.out.println("Subject\t\t: " + this.subject);
        System.out.println("Content\t\t: " + this.message);
    }
}

class SMTPClient {
    private final String host;
    private final String username;
    private final String password;

    public SMTPClient(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public void sendMessage(MailMessage mail) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", this.host);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(username, password);
            }
        };

        Session session = Session.getInstance(props, auth);

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(mail.getSender()));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(mail.getRecipient()));
        message.setSubject(mail.getSubject());
        message.setText(mail.getMessage());

        // Mengirim email
        Transport.send(message);
        System.out.println("Email terkirim!");
        mail.printMail();
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
            String recipient = message.getAllRecipients()[0].toString();
            String messageContent = message.getContent().toString();
            String sender = message.getFrom()[0].toString();
            String subject = message.getSubject();
            MailMessage mail = new MailMessage(messageContent, recipient, sender, subject);
            mails.add(mail);
        }

        return mails;

    }

}

public class MyEmailClient {
    public static void main(String[] args) {

        boolean running = true;
        Scanner sc = new Scanner(System.in);

        String email = null;
        String password = null;

        System.out.print("Masukkan alamat domain POP3\t: ");
        String pop3Host = sc.nextLine();

        System.out.print("Masukkan alamat domain SMTP\t: ");
        String smptHost = sc.nextLine();

        System.out.print("Masukkan email address\t: ");
        email = sc.nextLine();

        System.out.print("Masukkan password email\t: ");
        password = sc.nextLine();

        while (running) {
            System.out.println("\nPilih menu:");
            System.out.println("1. List pesan");
            System.out.println("2. Baca pesan tertentu");
            System.out.println("3. Kirim email.");
            System.out.println("4. Keluar.");

            System.out.print("PILIH [1-4]\t:");
            String pilihan = sc.nextLine();

            try {
                switch (pilihan.strip()) {
                    case "1":

                        System.out.print("Masukkan jumlah email yang diinginkan\t: ");
                        int jumlahEmail = sc.nextInt();

                        POP3Client pop3 = new POP3Client(pop3Host, email, password);
                        ArrayList<MailMessage> msg = pop3.getMessages(jumlahEmail);
                        msg.forEach((n) -> {
                            n.printMail();
                            System.out.println();
                        });
                        break;
                    case "2":
                        System.out.print("Masukkan jumlah email yang diinginkan\t: ");
                        int jumlah = sc.nextInt();

                        POP3Client pop32 = new POP3Client(pop3Host, email, password);
                        ArrayList<MailMessage> msgs = pop32.getMessages(jumlah);

                        System.out.println("SUBJECT");
                        System.out.println("=============");
                        for (int i = 0; i < msgs.size(); i++) {
                            int id = i + 1;
                            System.out.println();
                            System.out.println(id + ". " + msgs.get(i).getSubject());
                        }

                        System.out.print("Masukkan urutan email yang diinginkan\t: ");
                        int emailIndex = sc.nextInt();

                        msgs.get(emailIndex - 1).printMail();
                        break;
                    case "3":
                        System.out.print("Masukkan alamat email penerima: ");
                        String recipient = sc.nextLine();

                        System.out.print("Masukkan subject email: ");
                        String subject = sc.nextLine();

                        System.out.print("Masukkan konten email: ");
                        String content = sc.nextLine();
                        MailMessage mail = new MailMessage(content, recipient, email, subject);
                        SMTPClient smtpClient = new SMTPClient(smptHost, email,
                                password);
                        try {
                            smtpClient.sendMessage(mail);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        break;
                    case "4":
                        running = false;
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sc.close();
    }
}