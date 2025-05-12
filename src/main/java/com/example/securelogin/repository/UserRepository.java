package com.example.securelogin.repository;

import com.example.securelogin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // JpaRepository<User, Long> 中的：
    // - User: 是實體類型
    // - Long: 是主鍵的類型

    // 所有基本的 CRUD 操作都會自動提供：
    // - save(User entity)
    // - findById(Long id)
    // - findAll()
    // - delete(User entity)
    // 等等...
}
