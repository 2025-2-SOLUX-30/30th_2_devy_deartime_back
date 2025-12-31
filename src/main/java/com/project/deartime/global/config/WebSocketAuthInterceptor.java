package com.project.deartime.global.config;

import com.project.deartime.app.auth.controller.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * WebSocket STOMP 연결 시 JWT 토큰을 검증하는 인터셉터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // STOMP CONNECT 시 Authorization 헤더에서 JWT 토큰 추출
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    String userId = jwtTokenProvider.getUserId(token);
                    String email = jwtTokenProvider.getEmail(token);

                    // Authentication 객체 생성 및 설정
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );

                    accessor.setUser(authentication);
                    log.info("[WEBSOCKET] JWT 인증 성공. userId={}, email={}", userId, email);
                } else {
                    log.warn("[WEBSOCKET] 유효하지 않은 JWT 토큰");
                    throw new AccessDeniedException("유효하지 않은 JWT 토큰입니다.");
                }
            } else {
                log.warn("[WEBSOCKET] Authorization 헤더가 없거나 Bearer 형식이 아님");
                throw new AccessDeniedException("WebSocket 연결에 유효한 Authorization 헤더가 필요합니다.");
            }
        }

        return message;
    }
}

