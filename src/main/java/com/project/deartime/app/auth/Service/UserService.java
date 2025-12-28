package com.project.deartime.app.auth.Service;

import com.project.deartime.app.auth.dto.SignUpRequest;
import com.project.deartime.app.auth.dto.UpdateProfileRequest;
import com.project.deartime.app.auth.repository.UserRepository;
import com.project.deartime.app.domain.User;
import com.project.deartime.app.service.S3Service;
import com.project.deartime.global.exception.CoreApiException;
import com.project.deartime.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;  // 추가

    // MultipartFile 파라미터 추가
    public User signUp(String providerId, String email, SignUpRequest request, MultipartFile profileImage) {
        System.out.println("=== 회원가입 시작 ===");
        System.out.println("providerId: " + providerId);
        System.out.println("email: " + email);
        System.out.println("nickname: " + request.getNickname());

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CoreApiException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 프로필 이미지 업로드 처리
        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = s3Service.uploadFile(profileImage, "profiles");
            System.out.println("업로드된 이미지 URL: " + profileImageUrl);
        }

        User user = User.builder()
                .providerId(providerId)
                .email(email)
                .nickname(request.getNickname())
                .birthDate(request.getBirthDate())
                .bio(request.getBio())
                .profileImageUrl(profileImageUrl)  // S3 URL 저장
                .build();

        User savedUser = userRepository.save(user);
        System.out.println("저장된 User ID: " + savedUser.getId());
        System.out.println("=== 회원가입 완료 ===");

        return savedUser;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.USER_NOT_FOUND));
    }

    // MultipartFile 파라미터 추가
    public User updateProfile(Long userId, UpdateProfileRequest request, MultipartFile profileImage) {
        System.out.println("=== 프로필 업데이트 시작 ===");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 변경 시 중복 체크 (자기 자신은 제외)
        if (!user.getNickname().equals(request.getNickname())
                && userRepository.existsByNickname(request.getNickname())) {
            throw new CoreApiException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 프로필 이미지 업데이트 처리
        String profileImageUrl = user.getProfileImageUrl();  // 기존 URL 유지
        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 이미지가 있으면 삭제
            if (user.getProfileImageUrl() != null) {
                try {
                    s3Service.deleteFile(user.getProfileImageUrl());
                    System.out.println("기존 이미지 삭제 완료");
                } catch (Exception e) {
                    System.out.println("기존 이미지 삭제 실패: " + e.getMessage());
                }
            }

            // 새 이미지 업로드
            profileImageUrl = s3Service.uploadFile(profileImage, "profiles");
            System.out.println("새 이미지 업로드 완료: " + profileImageUrl);
        }

        user.updateProfile(
                request.getNickname(),
                request.getBirthDate(),
                request.getBio(),
                profileImageUrl
        );

        System.out.println("=== 프로필 업데이트 완료 ===");

        return user;
    }
}
