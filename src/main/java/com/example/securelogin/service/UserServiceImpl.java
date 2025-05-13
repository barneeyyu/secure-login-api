package com.example.securelogin.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.securelogin.repository.UserRepository;
import com.example.securelogin.repository.EmailVerificationTokenRepository;
import com.example.securelogin.security.JwtService;
import com.example.securelogin.entity.User;
import com.example.securelogin.entity.EmailVerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.securelogin.dto.RegisterRequest;
import com.example.securelogin.dto.LoginRequest;
import com.example.securelogin.dto.LoginVerifyRequest;
import com.example.securelogin.dto.LoginVerifyResponse;
import com.example.securelogin.dto.LastLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Optional;
import java.util.NoSuchElementException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.beans.factory.annotation.Qualifier;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository tokenRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtService jwtService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Qualifier("gmailService") EmailService emailService,
            EmailVerificationTokenRepository tokenRepository, TwoFactorAuthService twoFactorAuthService,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.twoFactorAuthService = twoFactorAuthService;
        this.jwtService = jwtService;
    }

    @Transactional
    @Override
    public void registerUser(RegisterRequest request) {
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
        emailService.sendRegistrationVerificationEmail(user.getEmail(), recipientName, tokenString);
        logger.info("User registered successfully with ID: {}, recipientName: {}", user.getId(), recipientName);
    }

    @Transactional
    @Override
    public void verifyRegistration(String tokenValue) {
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

    @Transactional
    @Override
    public void login(LoginRequest request) {
        logger.info("Attempting login for user: {}", request.getEmail());

        // 1. 根據電子郵件查找使用者
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            logger.warn("Login failed: User not found with email: {}", request.getEmail());
            throw new NoSuchElementException("the email is not registered");
        }

        User user = userOptional.get();

        // 2. 檢查電子郵件是否已驗證
        if (!user.isEmailVerified()) {
            logger.warn("Login failed: Email not verified for user: {}", request.getEmail());
            throw new IllegalStateException("Your email is not verified, please verify your email.");
        }

        // 3. 驗證密碼
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Invalid password for user: {}", request.getEmail());
            throw new BadCredentialsException("password is incorrect.");
        }

        // 4. 啟用兩步驟驗證，產生驗證碼
        String plainCode = twoFactorAuthService.createNewCode(user);
        logger.info("Two-factor authentication code generated for user: {}", plainCode); // 先把驗證碼印出來，等寄信功能好了再刪掉

        // 5. 發送驗證碼到使用者
        String recipientName = user.getEmail().split("@")[0];
        emailService.sendLoginVerificationCodeEmail(user.getEmail(), recipientName, plainCode);

        // 如果執行到這裡，表示帳號密碼驗證成功
        logger.info("User {} logged in successfully.", user.getEmail());
    }

    @Transactional
    @Override
    public LoginVerifyResponse loginVerify(LoginVerifyRequest request) {

        Boolean isCodeValid = twoFactorAuthService.verifyCode(request.getEmail(), request.getCode());

        if (!isCodeValid) {
            throw new BadCredentialsException("Invalid verification code");
        }

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        User user = userOptional.get();
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginVerifyResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    @Override
    public LastLoginResponse getLastLoginInfo(User user) {
        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());
        User resultUser = userOptional.get();
        if (resultUser.getLastLoginAt() == null) {
            throw new NoSuchElementException("User has not logged in yet");
        }

        // handle timezone
        ZoneId tz = ZoneId.of("Asia/Taipei");
        OffsetDateTime loginAt = resultUser.getLastLoginAt();
        String localTimeFormatted = loginAt
                .atZoneSameInstant(tz) // ★ 時區轉換核心
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z"));
        return LastLoginResponse.builder()
                .email(resultUser.getEmail())
                .lastLoginTime(localTimeFormatted)
                .build();
    }
}