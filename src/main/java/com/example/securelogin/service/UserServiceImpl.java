package com.example.securelogin.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.securelogin.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.securelogin.dto.RegisterRequest;
import com.example.securelogin.dto.LoginRequest;
import com.example.securelogin.security.UserPrincipal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.transaction.Transactional;
import com.example.securelogin.entity.User;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        User user = new User();
        user.setEmail(request.getEmail());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        logger.info("Attempting to save user: {}", user); // 印出整個 user 物件
        User savedUser = userRepository.save(user); // 將回傳值存起來
        logger.info("User saved successfully with ID: {}", savedUser.getId());
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