package com.example.securelogin.service;

import org.springframework.stereotype.Service;
import com.example.securelogin.dto.RegisterRequest;
import com.example.securelogin.dto.LoginRequest;
import com.example.securelogin.security.UserPrincipal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void registerUser(RegisterRequest request) {
        // TODO: 實作註冊邏輯
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

    @Override
    public void pingDB() {
        jdbcTemplate.queryForObject("SELECT 1", Integer.class);
    }
}