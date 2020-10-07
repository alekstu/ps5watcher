package fi.alekstu.ps5watch;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Emailer {

    private static final String ADMIN_EMAIL = "admin@email.com";

    private static final String GMAIL_USERNAME = "user@gmail.com";
    private static final String GMAIL_PASSWORD = "PASSWORD";
    //
    private static final String MAIL_TO = "receiver1@email.com, receiver2@email.com";


    // Get system properties
    private Properties properties;

    public Emailer() {
        properties = System.getProperties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
    }

    public void sendMail(final ListedConsole console) {
        final Session session = getMailSession();

        try {
            Message email = new MimeMessage(session);
            email.setFrom(new InternetAddress(GMAIL_USERNAME));
            email.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(MAIL_TO)
            );
            email.setSubject("PS5 saatavilla Prismasta!");
            email.setText(console.getName() + "\n\n" + console.getUrl() + "\n\n kaljoja odotellessa!");

            Transport.send(email);

        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    public void sendInfoMail(final String info) {
        final Session session = getMailSession();
        try {
            Message email = new MimeMessage(session);
            email.setFrom(new InternetAddress(GMAIL_USERNAME));
            email.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(ADMIN_EMAIL)
            );
            email.setSubject("Prisma PS5 - INFO");
            email.setText(info);

            Transport.send(email);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendCrashedMail(final String desc, Throwable throwable) {
        final Session session = getMailSession();
        try {
            Message email = new MimeMessage(session);
            email.setFrom(new InternetAddress(GMAIL_USERNAME));
            email.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(ADMIN_EMAIL)
            );
            email.setSubject("Prisma PS5 - Crashed");
            email.setText(desc + "\n\n" + throwable);

            Transport.send(email);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private Session getMailSession() {
        return Session.getInstance(properties,
                new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(GMAIL_USERNAME, GMAIL_PASSWORD);
                    }
                });
    }


}
