package com.project.deartime.app.auth.controller;

import com.project.deartime.app.auth.Service.UserService;
import com.project.deartime.app.auth.dto.SignUpRequest;
import com.project.deartime.app.auth.dto.UpdateProfileRequest;
import com.project.deartime.app.domain.User;
import com.project.deartime.global.dto.ApiResponseTemplete;
import com.project.deartime.global.exception.InvalidTokenException;
import com.project.deartime.global.exception.SuccessCode;
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
    public ResponseEntity<ApiResponseTemplete<Map<String, Object>>> signUp(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid SignUpRequest request,
            HttpServletResponse response
    ) {
        String tempToken = authHeader.replace("Bearer ", "");

        if (!jwtTokenProvider.validateToken(tempToken)) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
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

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("accessToken", accessToken);
        responseData.put("refreshToken", refreshToken);

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getId());
        userData.put("email", user.getEmail());
        userData.put("nickname", user.getNickname());
        userData.put("birthDate", user.getBirthDate());
        userData.put("bio", user.getBio());
        userData.put("profileImageUrl", user.getProfileImageUrl());

        responseData.put("user", userData);

        return ApiResponseTemplete.success(SuccessCode.SIGNUP_SUCCESS, responseData);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseTemplete<Map<String, Object>>> getMyInfo(
            @AuthenticationPrincipal String userId
    ) {
        System.out.println("=== 내 정보 조회 ===");
        System.out.println("userId: " + userId);

        User user = userService.getUserById(Long.parseLong(userId));

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getId());
        userData.put("email", user.getEmail());
        userData.put("nickname", user.getNickname());
        userData.put("birthDate", user.getBirthDate());
        userData.put("bio", user.getBio());
        userData.put("profileImageUrl", user.getProfileImageUrl());

        return ApiResponseTemplete.success(SuccessCode.USER_INFO_RETRIEVED, userData);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponseTemplete<Map<String, Object>>> updateMyProfile(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid UpdateProfileRequest request
    ) {
        System.out.println("=== 프로필 업데이트 ===");
        System.out.println("userId: " + userId);
        System.out.println("nickname: " + request.getNickname());
        System.out.println("birthDate: " + request.getBirthDate());

        User updatedUser = userService.updateProfile(Long.parseLong(userId), request);

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", updatedUser.getId());
        userData.put("email", updatedUser.getEmail());
        userData.put("nickname", updatedUser.getNickname());
        userData.put("birthDate", updatedUser.getBirthDate());
        userData.put("bio", updatedUser.getBio());
        userData.put("profileImageUrl", updatedUser.getProfileImageUrl());

        return ApiResponseTemplete.success(SuccessCode.PROFILE_UPDATE_SUCCESS, userData);
    }
}
