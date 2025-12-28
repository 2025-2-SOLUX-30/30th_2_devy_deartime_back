package com.project.deartime.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deartime.app.auth.Service.CustomOAuth2UserService;
import com.project.deartime.app.auth.controller.GoogleOAuth2SuccessHandler;
import com.project.deartime.app.auth.filter.JwtAuthenticationFilter;
import com.project.deartime.global.dto.ApiResponseTemplete;
import com.project.deartime.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/api/auth/google",
                                "/api/users/signup",
                                "/login/oauth2/code/**",
                                "/oauth2/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/logout").authenticated()
                        // 회원가입은 임시 토큰으로 접근
                        .requestMatchers("/api/users/signup").permitAll()
                        // 인증이 필요한 경로
                        .requestMatchers(
                                "/api/users/me",
                                "/api/friends/**",
                                "/api/letters/**",
                                "/api/timecapsules/**",
                                "/api/photos/**",
                                "/api/albums/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                // ✅ 예외 처리 핸들러 추가 (ErrorCode 사용)
                .exceptionHandling(exception -> exception
                        // 인증되지 않은 사용자 접근 시
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(ErrorCode.UNAUTHORIZED_EXCEPTION.getHttpStatusCode());
                            response.setContentType("application/json;charset=UTF-8");

                            ApiResponseTemplete<Object> errorResponse = ApiResponseTemplete.builder()
                                    .status(ErrorCode.UNAUTHORIZED_EXCEPTION.getHttpStatusCode())
                                    .success(false)
                                    .message(ErrorCode.UNAUTHORIZED_EXCEPTION.getMessage())
                                    .data(null)
                                    .build();

                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(errorResponse));
                        })
                        // 권한이 없는 사용자 접근 시
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(ErrorCode.ACCESS_DENIED_EXCEPTION.getHttpStatusCode());
                            response.setContentType("application/json;charset=UTF-8");

                            ApiResponseTemplete<Object> errorResponse = ApiResponseTemplete.builder()
                                    .status(ErrorCode.ACCESS_DENIED_EXCEPTION.getHttpStatusCode())
                                    .success(false)
                                    .message(ErrorCode.ACCESS_DENIED_EXCEPTION.getMessage())
                                    .data(null)
                                    .build();

                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(errorResponse));
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(googleOAuth2SuccessHandler)
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Refresh-Token"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}