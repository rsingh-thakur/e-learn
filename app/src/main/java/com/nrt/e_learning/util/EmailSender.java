package com.nrt.e_learning.util;
import android.os.AsyncTask;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    // Your email credentials and SMTP server information
    private static final String EMAIL_USERNAME = "basic.erps@gmail.com";
    private static final String EMAIL_PASSWORD = "Your_app_password";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;

    public static void sendEmail(String recipientEmail, String subject, String messageBody) {
        new SendEmailTask().execute(recipientEmail, subject, messageBody);
    }

    public static void sendOTPEmail(String recipientEmail, String otp) {
        String subject = "Your OTP Code";
        String messageBody = "Your OTP code is: " + otp;
        sendEmail(recipientEmail, subject, messageBody);
    }

    private static class SendEmailTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String recipientEmail = params[0];
            String subject = params[1];
            String messageBody = params[2];

            // Set up mail properties
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", SMTP_HOST);
            properties.put("mail.smtp.port", SMTP_PORT);

            // Create a mail session with authentication
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });

            try {
                // Create a message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_USERNAME));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
                message.setSubject(subject);
                message.setText(messageBody);

                // Send the message
                Transport transport = session.getTransport("smtp");
                transport.connect(SMTP_HOST, SMTP_PORT, EMAIL_USERNAME, EMAIL_PASSWORD);
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
