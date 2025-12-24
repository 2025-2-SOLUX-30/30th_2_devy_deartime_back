package com.project.deartime.app.auth.Service;

import com.project.deartime.app.auth.dto.SignUpRequest;
import com.project.deartime.app.auth.dto.UpdateProfileRequest;
import com.project.deartime.app.auth.repository.UserRepository;
import com.project.deartime.app.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public User signUp(String providerId, String email, SignUpRequest request) {

        System.out.println("=== 회원가입 시작 ===");
        System.out.println("providerId: " + providerId);
        System.out.println("email: " + email);
        System.out.println("nickname: " + request.getNickname());

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .providerId(providerId)
                .email(email)
                .nickname(request.getNickname())
                .birthDate(request.getBirthDate())
                .bio(request.getBio())
                .profileImageUrl(request.getProfileImageUrl())
                .build();

        User savedUser = userRepository.save(user);
        System.out.println("저장된 User ID: " + savedUser.getId());
        System.out.println("=== 회원가입 완료 ===");

        return savedUser;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    public User updateProfile(Long userId, UpdateProfileRequest request) {
        System.out.println("=== 프로필 업데이트 시작 ===");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 닉네임 변경 시 중복 체크 (자기 자신은 제외)
        if (!user.getNickname().equals(request.getNickname())
                && userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 기존 completeSignUp 메소드 활용
        user.completeSignUp(
                request.getNickname(),
                request.getBirthDate(),
                request.getBio(),
                request.getProfileImageUrl()
        );

        System.out.println("=== 프로필 업데이트 완료 ===");

        // @Transactional에 의해 자동으로 dirty checking 되어 저장됨
        return user;
    }
}
