package com.project.deartime.app.auth.controller;

import com.project.deartime.global.dto.ApiResponseTemplete;
import com.project.deartime.global.exception.SuccessCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @GetMapping("/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        // 스프링 시큐리티 표준 OAuth2 인증 시작 주소로 리다이렉트
        response.sendRedirect("/oauth2/authorization/google");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseTemplete<Void>> logout() {
        // 인증 정보 제거
        SecurityContextHolder.clearContext();

        return ApiResponseTemplete.success(SuccessCode.LOGOUT_SUCCESS, null);
    }
}
