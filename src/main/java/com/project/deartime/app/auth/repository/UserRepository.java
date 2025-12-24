package com.project.deartime.app.auth.repository;

import com.project.deartime.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderId(String providerId);

    boolean existsByNickname(String nickname);
}
