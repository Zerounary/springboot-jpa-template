package com.app.backend.repository;

import com.app.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Page<User> findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(String username, String nickname, Pageable pageable);
}
