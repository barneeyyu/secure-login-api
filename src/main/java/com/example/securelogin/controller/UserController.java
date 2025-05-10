package com.example.securelogin.controller;

import com.example.securelogin.dto.LoginRequest;
import com.example.securelogin.dto.RegisterRequest;
import com.example.securelogin.dto.LoginResponse;
import com.example.securelogin.security.UserPrincipal;
import com.example.securelogin.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    // 註冊：寄送驗證信
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok().body("Verification email sent.");
    }

    // 登入（帳密＋email 驗證碼）
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String jwtToken = userService.login(request);
        return ResponseEntity.ok(new LoginResponse(jwtToken));
    }

    // 查詢自己的最後登入時間（需身份驗證）
    @GetMapping("/user/last-login")
    public ResponseEntity<?> getLastLogin(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok().body(userService.getLastLoginInfo(userPrincipal));
    }


    // Ping DB 檢查連線
    @GetMapping("/ping-db")
    public ResponseEntity<?> pingDatabase() {
        try {
            userService.pingDB(); // 需在 service 層實作一個簡單的 ping 方法（如 select 1）
            return ResponseEntity.ok("✅ 成功連接到資料庫！");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 資料庫連線失敗：" + e.getMessage());
        }
    }

    // 測試 Beanstalk 連線
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Beanstalk!";
    }
}