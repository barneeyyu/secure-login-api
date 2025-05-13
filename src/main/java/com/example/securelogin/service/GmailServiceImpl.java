package com.example.securelogin.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import com.example.securelogin.config.AppProperties;

@Service("gmailService")
public class GmailServiceImpl implements EmailService {

        private static final Logger logger = LoggerFactory.getLogger(GmailServiceImpl.class);

        private final JavaMailSender mailSender;
        private final AppProperties app;

        @Value("${spring.mail.from}")
        private String configuredFromEmail;

        @Autowired
        public GmailServiceImpl(JavaMailSender mailSender, AppProperties app) {
                this.mailSender = mailSender;
                this.app = app;
        }

        @Override
        public void sendRegistrationVerificationEmail(String recipientEmail, String recipientName,
                        String verificationToken) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(configuredFromEmail);
                message.setTo(recipientEmail);
                message.setSubject("Verify Your Registration");

                String emailBody = String.format(
                                "Hello %s,\n\n" +
                                                "Thank you for registering with Our Secure Application.\n" +
                                                "Please click the link below to verify your email address:\n" +
                                                "%s\n\n" +
                                                "If you did not request this, please ignore this email.\n\n" +
                                                "Thanks,\nThe Application Team",
                                recipientName,
                                app.getBaseUrl() + "/api/verify-registration?token=" + verificationToken);
                message.setText(emailBody);

                mailSender.send(message);
                logger.info("Verification email sent successfully to {} via Gmail.", recipientEmail);
        }

        @Override
        public void sendLoginVerificationCodeEmail(String recipientEmail, String recipientName,
                        String verificationCode) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(configuredFromEmail);
                message.setTo(recipientEmail);
                // 設定郵件主旨，將驗證碼包含在主旨中
                String subject = String.format("【%s】is your login verification code", verificationCode);
                message.setSubject(subject);

                String emailBody = String.format(
                                "<p>Hello %s,</p>" +
                                                "<p>Your login verification code is【%s】</p>" +
                                                "<p>The code will expire in <b>5 minutes</b>.</p>" +
                                                "<p>If you did not request this, please ignore this email.</p>" +
                                                "<p>Thanks,<br>The Application Team</p>",
                                recipientName, verificationCode);
                message.setText(emailBody);

                mailSender.send(message);
                logger.info("Login verification code email sent successfully to {} via Gmail.", recipientEmail);
        }
}
