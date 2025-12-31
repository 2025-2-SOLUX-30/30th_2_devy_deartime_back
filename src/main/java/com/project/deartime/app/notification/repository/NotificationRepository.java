package com.project.deartime.app.notification.repository;

import com.project.deartime.app.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자의 모든 알림 조회 (읽지 않은 순, 최신순 정렬)
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.isRead ASC, n.createdAt DESC")
    Page<Notification> findByUserIdOrderByIsReadAscCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 모든 알림 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 읽지 않은 알림 목록 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);
}

