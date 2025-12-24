package com.project.deartime.app.auth.repository;

import com.project.deartime.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderId(String providerId);

    boolean existsByNickname(String nickname);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND u.id != :currentUserId")
    List<User> searchByNickname(@Param("keyword") String keyword,
                                @Param("currentUserId") Long currentUserId);
}
