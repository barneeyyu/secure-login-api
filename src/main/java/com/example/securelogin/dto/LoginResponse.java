package com.example.securelogin.dto;

public class LoginResponse {
    public String token;
    public LoginResponse(String token) {
        this.token = token;
    }
}
