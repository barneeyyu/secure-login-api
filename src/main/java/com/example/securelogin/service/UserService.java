package com.example.securelogin.service;

import com.example.securelogin.dto.RegisterRequest;
import com.example.securelogin.dto.LoginRequest;
import com.example.securelogin.dto.LoginVerifyRequest;
import com.example.securelogin.dto.LoginVerifyResponse;

public interface UserService {
    void registerUser(RegisterRequest request);

    void verifyRegistration(String tokenValue);

    void login(LoginRequest request);

    LoginVerifyResponse loginVerify(LoginVerifyRequest request);
}