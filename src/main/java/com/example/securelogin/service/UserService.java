package com.example.securelogin.service;

import com.example.securelogin.dto.RegisterRequest;
import com.example.securelogin.dto.LoginRequest;
import com.example.securelogin.security.UserPrincipal;
import java.util.Map;

public interface UserService {
    void registerUser(RegisterRequest request);
    String login(LoginRequest request);
    Map<String, Object> getLastLoginInfo(UserPrincipal userPrincipal);
    void pingDB();
}