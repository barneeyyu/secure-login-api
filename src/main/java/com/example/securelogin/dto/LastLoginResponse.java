package com.example.securelogin.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class LastLoginResponse {
    private String email;
    private String lastLoginTime;
}
