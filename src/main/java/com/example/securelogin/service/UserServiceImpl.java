package com.example.securelogin.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.securelogin.repository.UserRepository;
import com.example.securelogin.repository.EmailVerificationTokenRepository;
import com.example.securelogin.entity.User;
import com.example.securelogin.entity.EmailVerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.securelogin.dto.RegisterRequest;
import com.example.securelogin.dto.LoginRequest;
import com.example.securelogin.security.UserPrincipal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository tokenRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService,
            EmailVerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    @Override
    public void registerUser(RegisterRequest request) {
        // 在真實的應用中，這裡會包含：
        // 1. 檢查 Email 是否已存在 (如果存在，可能會拋出 EmailAlreadyExistsException)
        // 2. 密碼雜湊
        // 3. 建立 User 實體
        // 4. 儲存到資料庫
        // 5. 可能發送驗證郵件等

        // 1. 建立 User 實體並儲存
        User user = new User();
        user.setEmail(request.getEmail());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        User savedUser = userRepository.save(user);

        // 2. 產生token權杖，並儲存
        EmailVerificationToken token = new EmailVerificationToken();
        String tokenString = UUID.randomUUID().toString();
        token.setToken(tokenString);
        token.setUserId(savedUser.getId());
        token.setExpiresAt(OffsetDateTime.now().plusDays(1));
        tokenRepository.save(token);

        // 3. 發送驗證郵件
        String recipientName = user.getEmail().split("@")[0]; // split email from "@"
        // emailService.sendVerificationEmail(user.getEmail(), recipientName,
        // "http://localhost:8080/verify-email?token=" + tokenString);
        logger.info("User registered successfully with ID: {}, recipientName: {}", user.getId(), recipientName);
    }

    @Transactional
    @Override
    public void verifyEmail(String tokenValue) {
        logger.info("Attempting to verify email with token: {}", tokenValue);

        // 1. 查找權杖
        Optional<EmailVerificationToken> tokenOptional = tokenRepository.findByToken(tokenValue);

        if (tokenOptional.isEmpty()) {
            logger.warn("Invalid verification token provided: {}", tokenValue);
            throw new IllegalArgumentException("Invalid verification token provided");
        }

        EmailVerificationToken verificationToken = tokenOptional.get();

        // 2. 檢查權杖是否過期
        if (verificationToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            logger.warn("Verification token {} has expired. User ID: {}", tokenValue, verificationToken.getUserId());
            tokenRepository.delete(verificationToken); // 刪除過期的權杖
            throw new IllegalStateException("Verification token has expired");
        }

        // 3. 查找與權杖關聯的使用者
        Long userId = verificationToken.getUserId();
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            // 這種情況理論上不應該發生，如果權杖有效，使用者應該存在
            // 但作為防禦性程式設計，我們還是處理它
            logger.error("CRITICAL: User not found for a valid verification token. Token: {}, User ID: {}", tokenValue,
                    userId);
            // 即使使用者找不到，也刪除這個孤立的權杖
            tokenRepository.delete(verificationToken);
            throw new NoSuchElementException("User not found for a valid verification token");
        }

        User user = userOptional.get();

        // 4. 檢查使用者郵箱是否已驗證
        if (user.isEmailVerified()) { // 假設 User 實體有 isEmailVerified() getter
            logger.info("Email for user {} (ID: {}) was already verified. Consuming token {}.", user.getEmail(),
                    user.getId(), tokenValue);
            tokenRepository.delete(verificationToken); // 即使已驗證，也消耗掉當前使用的權杖
            throw new IllegalStateException("Email already verified");
        }

        // 5. 更新使用者郵箱驗證狀態
        user.setEmailVerified(true); // 假設 User 實體有 setEmailVerified(boolean) setter
        userRepository.save(user);
        logger.info("Email successfully verified for user {} (ID: {}).", user.getEmail(), user.getId());

        // 6. 刪除已使用的權杖
        tokenRepository.delete(verificationToken);
        logger.info("Verification token {} has been deleted after successful verification.", tokenValue);
    }

    @Override
    public String login(LoginRequest request) {
        // TODO: 實作登入邏輯
        return null;
    }

    @Override
    public Map<String, Object> getLastLoginInfo(UserPrincipal userPrincipal) {
        // TODO: 實作取得登入資訊邏輯
        return null;
    }
}