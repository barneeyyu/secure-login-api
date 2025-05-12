package com.example.securelogin.service;

import com.example.securelogin.entity.User;

public interface TwoFactorAuthService {
    String createNewCode(User user);

    boolean verifyCode(String email, String submittedCode);
}
