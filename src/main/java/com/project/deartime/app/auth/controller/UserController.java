package com.project.deartime.app.auth.controller;

import com.project.deartime.app.auth.Service.UserService;
import com.project.deartime.app.auth.dto.SignUpRequest;
import com.project.deartime.app.auth.dto.UpdateProfileRequest;
import com.project.deartime.app.domain.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid SignUpRequest request,
            HttpServletResponse response
    ) {
        String tempToken = authHeader.replace("Bearer ", "");

        if (!jwtTokenProvider.validateToken(tempToken)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 401);
            errorResponse.put("message", "유효하지 않은 토큰입니다.");
            return ResponseEntity.status(401).body(errorResponse);
        }

        String providerId = jwtTokenProvider.getProviderId(tempToken);
        String email = jwtTokenProvider.getEmail(tempToken);

        System.out.println("=== 토큰에서 추출한 정보 ===");
        System.out.println("providerId: " + providerId);
        System.out.println("email: " + email);

        User user = userService.signUp(providerId, email, request);

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

    // 내 정보 조회 엔드포인트 추가
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyInfo(
            @AuthenticationPrincipal String userId
    ) {
        System.out.println("=== 내 정보 조회 ===");
        System.out.println("userId: " + userId);

        User user = userService.getUserById(Long.parseLong(userId));

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", 200);
        responseBody.put("message", "조회 성공");

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

    //내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid UpdateProfileRequest request
    ) {
        System.out.println("=== 프로필 업데이트 ===");
        System.out.println("userId: " + userId);
        System.out.println("nickname: " + request.getNickname());
        System.out.println("birthDate: " + request.getBirthDate());

        User updatedUser = userService.updateProfile(Long.parseLong(userId), request);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", 200);
        responseBody.put("message", "프로필 업데이트 성공");

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", updatedUser.getId());
        userData.put("email", updatedUser.getEmail());
        userData.put("nickname", updatedUser.getNickname());
        userData.put("birthDate", updatedUser.getBirthDate());
        userData.put("bio", updatedUser.getBio());
        userData.put("profileImageUrl", updatedUser.getProfileImageUrl());

        responseBody.put("user", userData);

        return ResponseEntity.ok(responseBody);
    }
}