package com.example.securelogin.controller;

import com.example.securelogin.service.UserService;

import com.example.securelogin.dto.RegisterRequest;
import com.example.securelogin.dto.SuccessResponse;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String tokenValue) {
        userService.verifyEmail(tokenValue);
        return ResponseEntity.ok("Email verified successfully");
    }

    // @PostMapping("/login")
    // public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request)
    // {
    // String jwtToken = userService.login(request);
    // return ResponseEntity.ok(new LoginResponse(jwtToken));
    // }

    // // 查詢自己的最後登入時間（需身份驗證）
    // @GetMapping("/user/last-login")
    // public ResponseEntity<?> getLastLogin(@AuthenticationPrincipal UserPrincipal
    // userPrincipal) {
    // return ResponseEntity.ok().body(userService.getLastLoginInfo(userPrincipal));
    // }
}