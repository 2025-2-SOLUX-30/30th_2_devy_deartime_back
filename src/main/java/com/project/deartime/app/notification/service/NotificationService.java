package com.project.deartime.app.notification.service;

import com.project.deartime.app.domain.Notification;
import com.project.deartime.app.domain.User;
import com.project.deartime.app.notification.domain.NotificationType;
import com.project.deartime.app.notification.dto.NotificationResponse;
import com.project.deartime.app.notification.repository.NotificationRepository;
import com.project.deartime.global.dto.PageResponse;
import com.project.deartime.global.exception.CoreApiException;
import com.project.deartime.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String NOTIFICATION_DESTINATION = "/sub/notifications/";

    /**
     * 알림 생성 및 웹소켓 전송
     */
    @Transactional
    public NotificationResponse createAndSendNotification(
            User receiver,
            NotificationType type,
            String senderNickname,
            String contentTitle,
            Long targetId
    ) {
        String content = buildNotificationContent(type, senderNickname);

        Notification notification = Notification.builder()
                .user(receiver)
                .type(type)
                .content(content)
                .senderNickname(senderNickname)
                .contentTitle(contentTitle)
                .targetId(targetId)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse response = NotificationResponse.from(savedNotification);

        // 웹소켓으로 실시간 알림 전송
        sendWebSocketNotification(receiver.getId(), response);

        log.info("[NOTIFICATION] 알림 생성 및 전송 완료. userId={}, type={}, targetId={}",
                receiver.getId(), type, targetId);

        return response;
    }

    /**
     * 웹소켓으로 알림 전송
     */
    private void sendWebSocketNotification(Long userId, NotificationResponse notification) {
        try {
            messagingTemplate.convertAndSend(
                    NOTIFICATION_DESTINATION + userId,
                    notification
            );
            log.debug("[WEBSOCKET] 알림 전송 성공. userId={}", userId);
        } catch (Exception e) {
            log.error("[WEBSOCKET] 알림 전송 실패. userId={}", userId, e);
        }
    }

    /**
     * 알림 메시지 생성
     */
    private String buildNotificationContent(NotificationType type, String senderNickname) {
        String nickname = (senderNickname != null) ? senderNickname : "알 수 없음";
        return nickname + "님이 " + type.getDefaultMessage();
    }

    // ========== 알림 트리거 메서드들 ==========

    /**
     * 편지 수신 알림
     */
    @Transactional
    public void notifyLetterReceived(User receiver, Long letterId, String senderNickname, String letterTitle) {
        createAndSendNotification(
                receiver,
                NotificationType.LETTER_RECEIVED,
                senderNickname,
                letterTitle,
                letterId
        );
    }

    /**
     * 타임캡슐 수신 알림
     */
    @Transactional
    public void notifyCapsuleReceived(User receiver, Long capsuleId, String senderNickname, String capsuleTitle) {
        createAndSendNotification(
                receiver,
                NotificationType.CAPSULE_RECEIVED,
                senderNickname,
                capsuleTitle,
                capsuleId
        );
    }

    /**
     * 타임캡슐 오픈 알림
     */
    @Transactional
    public void notifyCapsuleOpened(User receiver, Long capsuleId, String senderNickname, String capsuleTitle) {
        createAndSendNotification(
                receiver,
                NotificationType.CAPSULE_OPENED,
                senderNickname,
                capsuleTitle,
                capsuleId
        );
    }

    /**
     * 친구 요청 알림
     */
    @Transactional
    public void notifyFriendRequest(User receiver, Long requesterId, String requesterNickname) {
        createAndSendNotification(
                receiver,
                NotificationType.FRIEND_REQUEST,
                requesterNickname,
                null,
                requesterId
        );
    }

    /**
     * 친구 수락 알림
     */
    @Transactional
    public void notifyFriendAccept(User receiver, Long accepterId, String accepterNickname) {
        createAndSendNotification(
                receiver,
                NotificationType.FRIEND_ACCEPT,
                accepterNickname,
                null,
                accepterId
        );
    }

    // ========== API 메서드들 ==========

    /**
     * 사용자의 알림 목록 조회 (읽지 않은 순)
     */
    public PageResponse<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        Page<Notification> notificationPage = notificationRepository
                .findByUserIdOrderByIsReadAscCreatedAtDesc(userId, pageable);

        Page<NotificationResponse> responsePage = notificationPage.map(NotificationResponse::from);
        return PageResponse.from(responsePage);
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 특정 알림 읽음 처리
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "알림을 찾을 수 없습니다. notificationId=" + notificationId));

        // 권한 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new CoreApiException(ErrorCode.ACCESS_DENIED_EXCEPTION,
                    "해당 알림에 접근할 권한이 없습니다.");
        }

        notification.markAsRead();

        return NotificationResponse.from(notification);
    }

    /**
     * 사용자의 모든 알림 삭제
     */
    @Transactional
    public void deleteAllNotifications(Long userId) {
        notificationRepository.deleteAllByUserId(userId);
        log.info("[NOTIFICATION] 모든 알림 삭제. userId={}", userId);
    }
}
