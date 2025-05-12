package com.example.securelogin.service;

import com.example.securelogin.entity.TwoFactorAuthCode;
import com.example.securelogin.entity.User;
import com.example.securelogin.repository.TwoFactorAuthCodeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

    private static final Logger logger = LoggerFactory.getLogger(TwoFactorAuthServiceImpl.class);
    private final TwoFactorAuthCodeRepository twoFactorAuthCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TwoFactorAuthServiceImpl(TwoFactorAuthCodeRepository twoFactorAuthCodeRepository,
            PasswordEncoder passwordEncoder) {
        this.twoFactorAuthCodeRepository = twoFactorAuthCodeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 建立新的驗證碼
    @Transactional
    @Override
    public String createNewCode(User user) {
        // 先刪除該使用者現有的已過期驗證碼，一個使用者最多只能有一個未使用的驗證碼
        List<TwoFactorAuthCode> existingCodes = twoFactorAuthCodeRepository
                .findByUserAndUsedFalse(user);
        twoFactorAuthCodeRepository.deleteAll(existingCodes);

        // 建立新的驗證碼
        String plainCode = generateRandomCode();
        TwoFactorAuthCode newCode = TwoFactorAuthCode.builder()
                .user(user)
                .codeHash(passwordEncoder.encode(plainCode))
                .expiresAt(OffsetDateTime.now().plusMinutes(5)) // 5分鐘後過期
                .used(false)
                .build();
        twoFactorAuthCodeRepository.save(newCode);
        return plainCode; // 回傳明碼
    }

    // 驗證驗證碼
    @Transactional
    @Override
    public boolean verifyCode(String email, String submittedCode) {
        logger.info("TwoFactorAuthServiceImpl: Verifying code for email: {}, submittedCode:{}", email,
                passwordEncoder.encode(submittedCode));
        Optional<TwoFactorAuthCode> codeOpt = twoFactorAuthCodeRepository
                .findActiveCodeByEmail(email, OffsetDateTime.now());

        if (codeOpt.isPresent()) {
            TwoFactorAuthCode code = codeOpt.get();
            if (passwordEncoder.matches(submittedCode, code.getCodeHash())) {
                code.setUsed(true);
                twoFactorAuthCodeRepository.save(code);
                return true;
            }
        }
        return false;
    }

    private String generateRandomCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}