package com.project.deartime.app.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TimeCapsule")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeCapsule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "theme", length = 50)
    private String theme;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "open_at", nullable = false)
    private LocalDateTime openAt;

    @Column(name = "is_notified", nullable = false)
    @Builder.Default
    private Boolean isNotified = false;

    // N:1 관계: senderId (보낸 사람)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // N:1 관계: receiverNickname (받는 사람)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /**
     * 알림 발송 완료 처리
     */
    public void markAsNotified() {
        this.isNotified = true;
    }
}
