package com.example.securelogin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVerifyResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}
