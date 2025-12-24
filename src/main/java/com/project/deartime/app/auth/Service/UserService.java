package com.project.deartime.app.auth.Service;

import com.project.deartime.app.auth.dto.SignUpRequest;
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

        return userRepository.save(user);
    }
}
