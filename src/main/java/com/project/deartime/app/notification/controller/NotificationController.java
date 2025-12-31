package com.project.deartime.app.notification.controller;

import com.project.deartime.app.notification.dto.NotificationResponse;
import com.project.deartime.app.notification.service.NotificationService;
import com.project.deartime.global.dto.ApiResponseTemplete;
import com.project.deartime.global.dto.PageResponse;
import com.project.deartime.global.exception.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 조회 (GET /api/notifications)
     * - 읽지 않은 알림 우선, 최신순 정렬 (Repository 쿼리에서 정렬)
     */
    @GetMapping
    public ResponseEntity<ApiResponseTemplete<PageResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userIdLong = Long.parseLong(userId);
        PageResponse<NotificationResponse> response = notificationService.getNotifications(userIdLong, pageable);

        return ApiResponseTemplete.success(
                SuccessCode.NOTIFICATION_LIST_SUCCESS,
                response
        );
    }

    /**
     * 읽지 않은 알림 개수 조회 (GET /api/notifications/unread-count)
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponseTemplete<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal String userId
    ) {
        Long userIdLong = Long.parseLong(userId);
        long count = notificationService.getUnreadCount(userIdLong);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);

        return ApiResponseTemplete.success(
                SuccessCode.NOTIFICATION_COUNT_SUCCESS,
                response
        );
    }

    /**
     * 특정 알림 읽음 처리 (PATCH /api/notifications/{id}/read)
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponseTemplete<NotificationResponse>> markAsRead(
            @PathVariable("id") Long notificationId,
            @AuthenticationPrincipal String userId
    ) {
        Long userIdLong = Long.parseLong(userId);
        NotificationResponse response = notificationService.markAsRead(notificationId, userIdLong);

        return ApiResponseTemplete.success(
                SuccessCode.NOTIFICATION_READ_SUCCESS,
                response
        );
    }

    /**
     * 알람 내역 전체 삭제 (DELETE /api/notifications)
     */
    @DeleteMapping
    public ResponseEntity<ApiResponseTemplete<Void>> deleteAllNotifications(
            @AuthenticationPrincipal String userId
    ) {
        Long userIdLong = Long.parseLong(userId);
        notificationService.deleteAllNotifications(userIdLong);

        return ApiResponseTemplete.success(
                SuccessCode.NOTIFICATION_DELETE_SUCCESS,
                null
        );
    }
}

