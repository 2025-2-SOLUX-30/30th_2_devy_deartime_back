package com.project.deartime.app.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL에서 BIGINT 자동 증가
    @Column(name = "id")
    private Long id;

    @Column(name = "provider_id", nullable = false, length = 50)
    private String providerId;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "nickname", nullable = false, length = 20, unique = true)
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "bio", length = 500)
    private String bio;

    /* =========================
       회원가입 완료 여부 판단
     ========================= */
    public boolean isRegistered() {
        return nickname != null;
    }

    /* =========================
       회원가입 정보 업데이트
     ========================= */
    public void completeSignUp(
            String nickname,
            LocalDate birthDate,
            String bio,
            String profileImageUrl
    ) {
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
    }

    /* =========================
   프로필 수정 (닉네임 등)
 ========================= */
    public void updateProfile(
            String nickname,
            LocalDate birthDate,
            String bio,
            String profileImageUrl
    ) {
        if (nickname != null) {
            this.nickname = nickname;
        }

        if (birthDate != null) {
            this.birthDate = birthDate;
        }

        if (bio != null) {
            this.bio = bio;
        }

        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }


    // 1. Letters: User 1명이 보낸 편지 N개
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Letter> sentLetters = new ArrayList<>();

    // 2. Letters: User 1명이 받은 편지 N개
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
    private List<Letter> receivedLetters = new ArrayList<>();

    // 3. LetterFavorite: User 1명이 좋아요 누른 LetterFavorite N개 (N:M 관계)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<LetterFavorite> favoriteLeters = new HashSet<>();

    // 4-1. TimeCapsule: User 1명이 보낸 TimeCapsule N개 (senderId)
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<TimeCapsule> sentTimeCapsules = new ArrayList<>();

    // 4-2. TimeCapsule: User 1명이 받은 TimeCapsule N개 (receiverNickname)
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
    private List<TimeCapsule> receivedTimeCapsules = new ArrayList<>();

    // 5. Proxies: User 1명이 프록시로 지정한 Proxy N개 (user_id)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Proxy> designatedProxies = new ArrayList<>(); // 권한을 위임한 사용자

    // 6. Proxies: User 1명이 대리인 역할을 하는 Proxy N개 (proxy_user_id)
    @OneToMany(mappedBy = "proxyUser", cascade = CascadeType.ALL)
    private List<Proxy> proxyForOthers = new ArrayList<>(); // 대리인 사용자

    // 7. Friend: User 1명이 친구 요청을 한 Friend N개 (user_id)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Friend> userFriendships = new HashSet<>();

    // 8. Friend: User 1명이 친구 요청을 받은 Friend N개 (friend_id)
    @OneToMany(mappedBy = "friend", cascade = CascadeType.ALL)
    private Set<Friend> friendOfUsers = new HashSet<>();

    // 9. Photos: User 1명이 업로드한 Photo N개
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Photo> uploadedPhotos = new HashSet<>();

    // 10. Albums: User 1명이 만든 Album N개
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Album> createdAlbums = new HashSet<>();

    // 11. Notification: User 1명이 받은 Notification N개
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Notification> receivedNotifications = new HashSet<>();
}
