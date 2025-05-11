package com.example.securelogin.controller;

import com.example.securelogin.service.UserService;

import com.example.securelogin.dto.RegisterRequest;
import com.example.securelogin.security.UserPrincipal;
import com.example.securelogin.dto.ErrorResponse;
import com.example.securelogin.dto.SuccessResponse;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // 註冊：寄送驗證信
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Controller: Received registration request for email: {}", request.getEmail());

        userService.registerUser(request);

        logger.info("Controller: UserService successfully processed registration for email: {}",
                request.getEmail());
        SuccessResponse<Map<String, String>> successResponse = new SuccessResponse<>(
                "User registration processed successfully. Please check your email for verification.",
                Map.of("email", request.getEmail(), "status", "PENDING_VERIFICATION"));
        return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
    }

    // 在 UserController.java 中
    // @GetMapping("/verify-email")
    // public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
    // boolean success = userService.verifyEmailToken(token);
    // if (success) {
    // // 選項 1: 回傳簡單訊息
    // // return ResponseEntity.ok("Email verified successfully! You can now
    // login.");

    // // 選項 3: 如果前端會處理，可以直接回傳成功狀態
    // return ResponseEntity.ok().body(Map.of("message", "Email verified
    // successfully!"));
    // } else {
    // return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired
    // verification token."));
    // }
    // }

    // 登入（帳密＋email 驗證碼）
    // @PostMapping("/login")
    // public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request)
    // {
    // String jwtToken = userService.login(request);
    // return ResponseEntity.ok(new LoginResponse(jwtToken));
    // }

    // 查詢自己的最後登入時間（需身份驗證）
    @GetMapping("/user/last-login")
    public ResponseEntity<?> getLastLogin(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok().body(userService.getLastLoginInfo(userPrincipal));
    }
}