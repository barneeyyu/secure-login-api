package com.example.securelogin.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "使用者登入驗證請求的資料模型")
@Data
public class LoginVerifyRequest {

    @Schema(description = "使用者Email", example = "test@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "驗證碼", example = "123456")
    @NotBlank(message = "Verification code cannot be blank")
    @Size(min = 6, max = 6, message = "Verification code must be 6 digits")
    private String code;
}
