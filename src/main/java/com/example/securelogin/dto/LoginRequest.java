package com.example.securelogin.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "使用者登入請求的資料模型")
@Data
public class LoginRequest {

    @Schema(description = "使用者Email", example = "test@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email format is invalid")
    private String email;

    @Schema(description = "使用者密碼", example = "123456789")
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
