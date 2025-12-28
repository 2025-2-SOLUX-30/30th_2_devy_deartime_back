package com.project.deartime.app.friend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.deartime.app.domain.Proxy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyResponseDto {

    private Long proxyId;
    private Long userId;
    private String userNickname;
    private Long proxyUserId;
    private String proxyUserNickname;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiredAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static ProxyResponseDto from(Proxy proxy) {
        return ProxyResponseDto.builder()
                .proxyId(proxy.getId())
                .userId(proxy.getUser().getId())
                .userNickname(proxy.getUser().getNickname())
                .proxyUserId(proxy.getProxyUser().getId())
                .proxyUserNickname(proxy.getProxyUser().getNickname())
                .expiredAt(proxy.getExpiredAt())
                .createdAt(proxy.getCreatedAt())
                .build();
    }
}