package com.project.deartime.app.auth.controller;

import com.project.deartime.app.auth.repository.UserRepository;
import com.project.deartime.app.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");

        var optionalUser = userRepository.findByProviderId(providerId);

        if (optionalUser.isPresent() && optionalUser.get().isRegistered()) {
            // 기존 유저 - 정식 토큰 발급
            User user = optionalUser.get();

            String accessToken = jwtTokenProvider.createAccessToken(user.getId().toString(), user.getEmail());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getId().toString());

            System.out.println("=== 로그인 성공 ===");
            System.out.println("Access Token: " + accessToken);
            System.out.println("Refresh Token: " + refreshToken);

            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.println("{");
            writer.println("  \"status\": 200,");
            writer.println("  \"message\": \"로그인 성공\",");
            writer.println("  \"accessToken\": \"" + accessToken + "\",");
            writer.println("  \"refreshToken\": \"" + refreshToken + "\"");
            writer.println("}");
            writer.flush();
        } else {
            // 신규 유저 - 임시 토큰 발급 (회원가입용)
            String tempToken = jwtTokenProvider.createTempToken(providerId, email);

            System.out.println("=== 신규 유저 - 임시 토큰 발급 ===");
            System.out.println("Temp Token: " + tempToken);

            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.println("{");
            writer.println("  \"status\": 200,");
            writer.println("  \"message\": \"신규 유저 (회원가입 필요)\",");
            writer.println("  \"tempToken\": \"" + tempToken + "\"");
            writer.println("}");
            writer.flush();
        }
    }
}