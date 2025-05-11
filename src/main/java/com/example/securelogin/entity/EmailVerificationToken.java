package com.example.securelogin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt; // 使用 OffsetDateTime 來對應 TIMESTAMP WITH TIME ZONE

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public EmailVerificationToken() {
    }

    public EmailVerificationToken(String token, Long userId, OffsetDateTime expiresAt) {
        this(); // 呼叫無參構造函數設定 createdAt
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }
}