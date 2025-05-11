package com.example.securelogin.service;

public interface EmailService {

    /**
     * 發送驗證郵件
     *
     * @param recipientEmail   收件人 Email 地址
     * @param recipientName    收件人名稱
     * @param verificationLink 驗證連結
     */
    void sendVerificationEmail(String recipientEmail, String recipientName, String verificationLink);
}
