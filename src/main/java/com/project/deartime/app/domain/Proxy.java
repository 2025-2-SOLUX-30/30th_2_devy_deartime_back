package com.project.deartime.app.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Proxies")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Proxy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 관계: proxy_user_id (대리인 역할을 하는 사용자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proxy_user_id", nullable = false)
    private User proxyUser;

    // N:1 관계: user_id (권한을 위임한 원래 사용자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
