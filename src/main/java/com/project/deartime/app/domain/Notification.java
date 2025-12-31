package com.project.deartime.app.domain;

import com.project.deartime.app.notification.domain.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Notification")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // N:1 관계: user_id (알림을 받는 사용자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "content", nullable = false, length = 255)
    private String content; // 알림 메시지 (예: "{보낸이}님이 편지를 보냈습니다.")

    @Column(name = "target_id")
    private Long targetId; // 클릭 시 이동할 리소스의 ID (편지 ID, 타임캡슐 ID 등)

    @Column(name = "content_title", length = 100)
    private String contentTitle; // 관련 리소스의 제목 (편지 제목, 캡슐 제목 등)

    @Column(name = "sender_nickname", length = 20)
    private String senderNickname; // 알림을 발생시킨 유저의 닉네임

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * 알림 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }
}
