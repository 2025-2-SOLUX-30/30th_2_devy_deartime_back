package com.project.deartime.app.auth.controller;

import com.project.deartime.app.auth.Service.UserService;
import com.project.deartime.app.auth.dto.SignUpRequest;
import com.project.deartime.app.domain.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(
            @RequestHeader("Authorization") String authHeader,  // 헤더에서 토큰 받기
            @RequestBody @Valid SignUpRequest request,
            HttpServletResponse response
    ) {
        // "Bearer " 제거하고 토큰만 추출
        String tempToken = authHeader.replace("Bearer ", "");

        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(tempToken)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 401);
            errorResponse.put("message", "유효하지 않은 토큰입니다.");
            return ResponseEntity.status(401).body(errorResponse);
        }

        // 토큰에서 providerId와 email 추출
        String providerId = jwtTokenProvider.getProviderId(tempToken);
        String email = jwtTokenProvider.getEmail(tempToken);

        System.out.println("=== 토큰에서 추출한 정보 ===");
        System.out.println("providerId: " + providerId);
        System.out.println("email: " + email);

        // 회원가입 처리
        User user = userService.signUp(providerId, email, request);

        // 정식 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId().toString(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId().toString());

        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("Refresh-Token", refreshToken);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", 200);
        responseBody.put("message", "회원가입 성공");
        responseBody.put("accessToken", accessToken);
        responseBody.put("refreshToken", refreshToken);

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getId());
        userData.put("email", user.getEmail());
        userData.put("nickname", user.getNickname());
        userData.put("birthDate", user.getBirthDate());
        userData.put("bio", user.getBio());
        userData.put("profileImageUrl", user.getProfileImageUrl());

        responseBody.put("user", userData);

        return ResponseEntity.ok(responseBody);
    }
}
