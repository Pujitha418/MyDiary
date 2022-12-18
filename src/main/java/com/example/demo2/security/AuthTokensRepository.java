package com.example.demo2.security;

import com.example.demo2.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuthTokensRepository extends JpaRepository<AuthTokens, UUID> {
    List<AuthTokens> findAllByUser(User user);
}
