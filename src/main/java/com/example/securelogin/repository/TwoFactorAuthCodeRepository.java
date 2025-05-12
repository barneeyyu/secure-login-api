package com.example.securelogin.repository;

import com.example.securelogin.entity.TwoFactorAuthCode;
import com.example.securelogin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;

public interface TwoFactorAuthCodeRepository extends JpaRepository<TwoFactorAuthCode, Long> {

        // 查找使用者所有未使用的驗證碼，以便在創建新的驗證碼時刪除
        List<TwoFactorAuthCode> findByUserAndUsedFalse(
                        User user);

        // 使用 JPQL 查詢來查找驗證碼
        @Query("SELECT t FROM TwoFactorAuthCode t " +
                        "JOIN FETCH t.user u " + // JOIN FETCH 同時加載 User，避免 N+1 問題
                        "WHERE u.email = :email " +
                        "AND t.used = false " +
                        "AND t.expiresAt > :now")
        Optional<TwoFactorAuthCode> findActiveCodeByEmail(@Param("email") String email,
                        @Param("now") OffsetDateTime now);
}