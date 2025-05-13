package com.example.securelogin.dto;

import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "使用者登入驗證回應的資料模型")
@Data
@Builder
public class LoginVerifyResponse {
    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "JWT refresh token")
    private String refreshToken;

    @Schema(description = "token type")
    private String tokenType;
}
