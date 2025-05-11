package com.example.securelogin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "mailjet")
public class MailjetConfig {

    private String apiKey;
    private String secretKey;
    private String senderEmail;
    private String senderName;
}
