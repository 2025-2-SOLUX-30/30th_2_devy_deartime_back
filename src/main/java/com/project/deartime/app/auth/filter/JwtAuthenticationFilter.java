package com.project.deartime.app.auth.filter;

import com.project.deartime.app.auth.controller.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        System.out.println("=== JWT Filter 실행 ===");
        System.out.println("요청 URI: " + requestURI);
        System.out.println("Authorization 헤더: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            System.out.println("토큰 추출 완료: " + token.substring(0, Math.min(20, token.length())) + "...");

            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String userId = jwtTokenProvider.getUserId(token);

                    System.out.println("✅ 토큰 유효 - 사용자 ID: " + userId);

                    // 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    new ArrayList<>()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // SecurityContext에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    System.out.println("✅ SecurityContext에 인증 정보 저장 완료");
                } else {
                    System.out.println("❌ 토큰 유효성 검증 실패");
                }
            } catch (Exception e) {
                System.out.println("❌ 토큰 처리 중 오류: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ Authorization 헤더 없음 또는 Bearer 형식 아님");
        }

        filterChain.doFilter(request, response);
    }
}