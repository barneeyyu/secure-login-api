package com.example.securelogin.service;

import com.example.securelogin.config.MailjetConfig;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.securelogin.config.AppProperties;

@Service("mailjetEmailService")
public class MailjetEmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(MailjetEmailServiceImpl.class);
    private final MailjetClient client;
    private final MailjetConfig mailjetConfig;
    private final AppProperties app;

    @Autowired
    public MailjetEmailServiceImpl(MailjetConfig mailjetConfig, AppProperties app) {
        this.mailjetConfig = mailjetConfig;
        this.app = app;
        logger.info("Loaded Mailjet API Key from config: [{}]", mailjetConfig.getApiKey()); // 檢查這裡的值
        logger.info("Loaded Mailjet Secret Key from config: [{}]", mailjetConfig.getSecretKey()); // 檢查這裡的值
        ClientOptions options = ClientOptions.builder()
                .apiKey(mailjetConfig.getApiKey())
                .apiSecretKey(mailjetConfig.getSecretKey())
                .build();
        this.client = new MailjetClient(options);
        logger.info("MailjetClient initialized for email service. API Key Loaded: {}",
                mailjetConfig.getApiKey() != null && !mailjetConfig.getApiKey().isEmpty());
    }

    @Override
    public void sendRegistrationVerificationEmail(String recipientEmail, String recipientName, String token) {
        MailjetRequest request;
        MailjetResponse response;

        String emailSubject = "Verify Your Email Address for " + mailjetConfig.getSenderName();
        // 保持郵件內容的 HTML 和純文字版本
        String emailHtmlContent = String.format(
                "<h1>Welcome to %s!</h1>" +
                        "<p>Hi %s,</p>" +
                        "<p>Thank you for registering. Please click the link below to verify your email address:</p>"
                        +
                        "<p><a href=\"%s\">Verify Email</a></p>" +
                        "<p>If you did not register, please ignore this email.</p>" +
                        "<p>Thanks,<br/>The %s Team</p>",
                mailjetConfig.getSenderName(), recipientName,
                app.getBaseUrl() + "/api/verify-registration?token=" + token,
                mailjetConfig.getSenderName());
        String emailTextContent = String.format(
                "Welcome to %s!\n\n" +
                        "Hi %s,\n\n" +
                        "Thank you for registering. Please copy and paste the link below into your browser to verify your email address:\n"
                        +
                        "%s\n\n" +
                        "If you did not register, please ignore this email.\n\n" +
                        "Thanks,\nThe %s Team",
                mailjetConfig.getSenderName(), recipientName,
                app.getBaseUrl() + "/api/verify-registration?token=" + token,
                mailjetConfig.getSenderName());

        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", mailjetConfig
                                                .getSenderEmail()) // From
                                                                   // config
                                        .put("Name", mailjetConfig
                                                .getSenderName())) // From
                                                                   // config
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", recipientEmail) // Dynamic
                                                                              // parameter
                                                .put("Name", recipientName))) // Dynamic
                                                                              // parameter
                                .put(Emailv31.Message.SUBJECT, emailSubject)
                                .put(Emailv31.Message.TEXTPART, emailTextContent)
                                .put(Emailv31.Message.HTMLPART, emailHtmlContent)
                        // Optional: Add CustomID for tracking
                        // .put(Emailv31.Message.CUSTOMID, "AppVerificationEmail-" +
                        // System.currentTimeMillis())
                        ));
        try {
            logger.info("Attempting to send verification email to: {}", recipientEmail);
            response = client.post(request); // API call
            logger.info("Mailjet API Response Status for {}: {}", recipientEmail, response.getStatus());

            if (logger.isDebugEnabled()) {
                logger.debug("Mailjet API Response Data for {}: {}", recipientEmail,
                        response.getData());
            }

            // Check response status, throw custom exception for transaction rollback
            if (response.getStatus() != 200) { // Mailjet success is typically 200
                String errorMessage = String.format(
                        "Failed to send verification email to %s. Status: %d, Data: %s",
                        recipientEmail, response.getStatus(),
                        response.getData().toString().substring(0,
                                Math.min(response.getData().toString().length(), 500))); // Limit
                                                                                         // error
                                                                                         // message
                                                                                         // length
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            logger.info("Verification email sent successfully to {}", recipientEmail);

        } catch (MailjetException e) { // Catches MailjetException and its subclasses like
                                       // MailjetSocketTimeoutException
            logger.error("MailjetException while sending email to {}: {}", recipientEmail, e.getMessage(),
                    e);
            // Wrap and re-throw as a RuntimeException for transaction handling
            throw new RuntimeException("Mailjet API error while sending email to "
                    + recipientEmail + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void sendLoginVerificationCodeEmail(String recipientEmail, String recipientName, String verificationCode) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendLoginVerificationCodeEmail'");
    }
}
