package com.project.deartime.app.auth.Service;

import com.project.deartime.app.auth.repository.UserRepository;
import com.project.deartime.app.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String providerId = attributes.get("sub").toString();
        String email = attributes.get("email").toString();

        // DB에 사용자 존재 여부 확인
        Optional<User> existingUser = userRepository.findByProviderId(providerId);

        // 존재하지 않으면 회원가입 화면으로 이동하도록 SuccessHandler에서 처리
        // 여기서는 OAuth2User 반환만
        return oauth2User;
    }
}
