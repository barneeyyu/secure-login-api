package com.example.securelogin.dto;

import lombok.Data;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "查詢自己的最後登入時間回應的資料模型")
@Data
@Builder
public class LastLoginResponse {
    private String email;
    private String lastLoginTime;
}
