package com.example.securelogin.controller;

import com.example.securelogin.service.UserService;

import com.example.securelogin.dto.RegisterRequest;
import com.example.securelogin.dto.RegisterResponse;
import com.example.securelogin.entity.User;
import com.example.securelogin.dto.LoginRequest;
import com.example.securelogin.dto.LoginVerifyRequest;
import com.example.securelogin.dto.LoginVerifyResponse;
import com.example.securelogin.dto.LastLoginResponse;
import com.example.securelogin.dto.ErrorResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Secure Login Service", description = "使用者註冊與登入相關API")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "使用者註冊", description = "接收使用者Email、密碼，註冊成功寄驗證信")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "註冊請求成功，請檢查 Email 完成驗證", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "輸入驗證失敗 (例如 Email 格式錯誤、密碼太短)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email 已被註冊", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Controller: Received registration request for email: {}", request.getEmail());

        userService.registerUser(request);

        logger.info("Controller: UserService successfully processed registration for email: {}",
                request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterResponse.builder()
                .email(request.getEmail())
                .status("PENDING_VERIFICATION")
                .message("User registration processed successfully. Please check your email for verification.")
                .build());
    }

    @Operation(summary = "使用者註冊驗證", description = "會透過email發信把該連結附上，並且點擊連結後會驗證成功，帳號正式啟用")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "驗證成功，返回成功字串"),
            @ApiResponse(responseCode = "400", description = "驗證失敗，token不存在或已過期", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "驗證失敗，找不到使用者", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "伺服器錯誤", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/verify-registration")
    public ResponseEntity<?> verifyRegistration(@RequestParam("token") String tokenValue) {
        userService.verifyRegistration(tokenValue);
        return ResponseEntity.ok("Email verified successfully");
    }

    @Operation(summary = "使用者登入", description = "接收使用者Email、密碼，登入成功寄6位驗證碼的驗證信")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登入成功，返回成功字串"),
            @ApiResponse(responseCode = "400", description = "請求email格式錯誤或密碼不符合規則；或是帳號未驗證", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "帳號或密碼錯誤", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "伺服器錯誤", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        userService.login(request);
        return ResponseEntity.ok("Go to your email to get the verification code");
    }

    @Operation(summary = "使用者登入驗證", description = "接收使用者Email、驗證碼，驗證成功返回JWT token，並記錄登入時間")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "驗證登入成功，返回JWT token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginVerifyResponse.class))),
            @ApiResponse(responseCode = "400", description = "請求email格式錯誤", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "驗證碼錯誤", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "伺服器錯誤", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login-verify")
    public ResponseEntity<?> loginVerify(@Valid @RequestBody LoginVerifyRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.loginVerify(request));
    }

    @Operation(summary = "查詢自己的最後登入時間", description = "只允許已登入的使用者查詢自己的最後登入時間，使用者需使用JWT access token當作身分驗證")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查詢成功，返回最後登入時間", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LastLoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "未授權", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "找不到使用者，或使用者尚未有登入紀錄", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "伺服器錯誤", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/last-login")
    public ResponseEntity<?> getLastLogin(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(userService.getLastLoginInfo(user));
    }
}