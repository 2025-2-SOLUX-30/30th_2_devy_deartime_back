package com.project.deartime.app.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deartime.app.auth.controller.JwtTokenProvider;
import com.project.deartime.global.dto.ApiResponseTemplete;
import com.project.deartime.global.exception.ErrorCode;
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

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                System.out.println("토큰 추출 완료: " + token.substring(0, Math.min(20, token.length())) + "...");

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
                    // ✅ ErrorCode.INVALID_TOKEN 사용
                    sendErrorResponse(response, ErrorCode.INVALID_TOKEN);
                    return; // 필터 체인 중단
                }
            } else {
                System.out.println("⚠️ Authorization 헤더 없음 또는 Bearer 형식 아님");
            }

            filterChain.doFilter(request, response);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // ✅ ErrorCode.EXPIRED_TOKEN_EXCEPTION 사용
            System.out.println("❌ 토큰 만료: " + e.getMessage());
            sendErrorResponse(response, ErrorCode.EXPIRED_TOKEN_EXCEPTION);

        } catch (io.jsonwebtoken.security.SignatureException e) {
            // ✅ ErrorCode.INVALID_SIGNATURE_EXCEPTION 사용
            System.out.println("❌ JWT 서명 오류: " + e.getMessage());
            sendErrorResponse(response, ErrorCode.INVALID_SIGNATURE_EXCEPTION);

        } catch (io.jsonwebtoken.JwtException e) {
            // ✅ ErrorCode.INVALID_TOKEN 사용
            System.out.println("❌ JWT 예외: " + e.getMessage());
            sendErrorResponse(response, ErrorCode.INVALID_TOKEN);

        } catch (Exception e) {
            // ✅ ErrorCode.INTERNAL_SERVER_ERROR 사용
            System.out.println("❌ 토큰 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ ErrorCode를 받아서 에러 응답 전송
    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatusCode());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponseTemplete<Object> errorResponse = ApiResponseTemplete.builder()
                .status(errorCode.getHttpStatusCode())
                .success(false)
                .message(errorCode.getMessage())
                .data(null)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
