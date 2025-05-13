package com.example.securelogin.service;

public interface EmailService {

    /**
     * 發送註冊驗證郵件
     *
     * @param recipientEmail    收件人 Email 地址
     * @param recipientName     收件人名稱
     * @param verificationToken 驗證權杖
     */
    void sendRegistrationVerificationEmail(String recipientEmail, String recipientName, String verificationToken);

    /**
     * 發送登入驗證碼郵件
     *
     * @param recipientEmail   收件人 Email 地址
     * @param recipientName    收件人名稱
     * @param verificationCode 驗證碼
     */
    void sendLoginVerificationCodeEmail(String recipientEmail, String recipientName, String verificationCode);
}
