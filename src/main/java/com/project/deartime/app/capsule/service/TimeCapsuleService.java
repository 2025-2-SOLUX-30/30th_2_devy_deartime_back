package com.project.deartime.app.capsule.service;

import com.project.deartime.app.capsule.dto.CapsuleResponse;
import com.project.deartime.app.capsule.dto.CapsuleType;
import com.project.deartime.app.capsule.dto.CreateCapsuleRequest;
import com.project.deartime.app.capsule.repository.TimeCapsuleRepository;
import com.project.deartime.app.domain.TimeCapsule;
import com.project.deartime.app.domain.User;
import com.project.deartime.app.friend.repository.FriendRepository;
import com.project.deartime.app.auth.repository.UserRepository;
import com.project.deartime.app.notification.service.NotificationService;
import com.project.deartime.app.service.S3Service;
import com.project.deartime.global.exception.CoreApiException;
import com.project.deartime.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeCapsuleService {

    private final TimeCapsuleRepository timeCapsuleRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;

    private static final String CAPSULE_FOLDER = "capsules";

    /**
     * 타임캡슐 생성
     *
     * @param senderId 보낸 사람 ID
     * @param request 캡슐 생성 요청
     * @param imageFile 이미지 파일 (선택)
     * @return 생성된 캡슐 응답
     */
    @Transactional
    public CapsuleResponse createCapsule(Long senderId, CreateCapsuleRequest request, MultipartFile imageFile, User senderUser) {
        // 받는 사람이 존재하는지 확인
        if (senderId.equals(request.getReceiverId())) {
            throw new CoreApiException(ErrorCode.CAPSULE_SELF_SEND);
        }

        // 친구 관계 확인
        boolean isFriend = friendRepository.existsByUserIdAndFriendIdAndStatus(
                senderId,
                request.getReceiverId(),
                "accepted"
        );

        if (!isFriend) {
            throw new CoreApiException(ErrorCode.CAPSULE_RECEIVER_NOT_FRIEND);
        }

        // 받는 사람 조회
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new CoreApiException(ErrorCode.USER_NOT_FOUND));

        // 이미지 업로드 (선택)
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = s3Service.uploadFile(imageFile, CAPSULE_FOLDER);
        }

        try {
            // 캡슐 생성
            TimeCapsule capsule = TimeCapsule.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .theme(request.getTheme())
                    .imageUrl(imageUrl)
                    .openAt(request.getOpenAt())
                    .sender(senderUser)
                    .receiver(receiver)
                    .isNotified(false)
                    .build();

            TimeCapsule savedCapsule = timeCapsuleRepository.save(capsule);

            // 수신자에게 알림 발송 (캡슐 제목 포함)
            try {
                notificationService.notifyCapsuleReceived(
                        receiver,
                        savedCapsule.getId(),
                        senderUser.getNickname(),
                        savedCapsule.getTitle()
                );
            } catch (Exception e) {
                log.error("[CAPSULE] 알림 발송 실패. capsuleId={}", savedCapsule.getId(), e);
            }

            log.info("[CAPSULE] 타임캡슐 생성. capsuleId={}, senderId={}", savedCapsule.getId(), senderId);

            return CapsuleResponse.from(savedCapsule, true);
        } catch (Exception e) {
            // DB 저장 실패 시 S3에 업로드된 이미지 삭제 시도
            // 주의: S3 삭제도 실패할 경우 orphan 파일이 남을 수 있음 (별도 정리 작업 필요)
            if (imageUrl != null) {
                try {
                    s3Service.deleteFile(imageUrl);
                    log.info("[CAPSULE] S3 이미지 롤백 완료. imageUrl={}", imageUrl);
                } catch (Exception s3Exception) {
                    // S3 삭제 실패 시 경고 레벨로 로깅 (orphan 파일 발생)
                    log.warn("[CAPSULE] S3 이미지 롤백 실패 - orphan 파일 발생 가능. imageUrl={}", imageUrl, s3Exception);
                }
            }
            throw e;
        }
    }

    /**
     * 캡슐 목록 조회 (필터링)
     *
     * @param userId 조회 사용자 ID
     * @param type 캡슐 타입
     * @param pageable 페이징 정보
     * @return 필터링된 캡슐 목록
     */
    @Transactional(readOnly = true)
    public Page<CapsuleResponse> getCapsulesByType(Long userId, CapsuleType type, Pageable pageable) {
        Page<TimeCapsule> capsules;

        if (type == null) {
            type = CapsuleType.ALL;
        }

        switch (type) {
            case ALL:
                capsules = timeCapsuleRepository.findAllCapsules(userId, pageable);
                break;
            case RECEIVED:
                capsules = timeCapsuleRepository.findByReceiverId(userId, pageable);
                break;
            case SENT:
                capsules = timeCapsuleRepository.findBySenderId(userId, pageable);
                break;
            case OPENED:
                capsules = timeCapsuleRepository.findOpenedCapsules(userId, pageable);
                break;
            default:
                throw new CoreApiException(ErrorCode.INVALID_CAPSULE_TYPE);
        }

        return capsules.map(capsule -> CapsuleResponse.from(capsule, canAccessCapsule(userId, capsule)));
    }

    /**
     * 캡슐 상세 조회
     *
     * @param capsuleId 캡슐 ID
     * @param userId 조회 사용자 ID
     * @return 캡슐 상세 정보
     */
    @Transactional(readOnly = true)
    public CapsuleResponse getCapsule(Long capsuleId, Long userId) {
        TimeCapsule capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.CAPSULE_NOT_FOUND));

        // 1. 접근 권한이 없는 경우 예외 발생 (관계 확인 먼저)
        if (!isUserRelatedToCapsule(userId, capsule)) {
            throw new CoreApiException(ErrorCode.CAPSULE_ACCESS_DENIED);
        }

        // 2. 받는 사람이고 아직 열어볼 수 없는 경우 예외 발생
        boolean isReceiver = capsule.getReceiver().getId().equals(userId);
        boolean isNotOpened = LocalDateTime.now().isBefore(capsule.getOpenAt());
        if (isReceiver && isNotOpened) {
            throw new CoreApiException(ErrorCode.CAPSULE_NOT_OPENED);
        }

        // 3. 최종 접근 권한 확인
        boolean canAccess = canAccessCapsule(userId, capsule);

        return CapsuleResponse.from(capsule, canAccess);
    }

    /**
     * 사용자가 캡슐에 접근할 수 있는지 확인
     * - 보낸 사람은 항상 접근 가능
     * - 받는 사람은 openAt 이후에만 접근 가능
     */
    private boolean canAccessCapsule(Long userId, TimeCapsule capsule) {
        // 보낸 사람은 항상 접근 가능
        if (capsule.getSender().getId().equals(userId)) {
            return true;
        }

        // 받는 사람은 openAt 시간 이후(openAt 포함)에 접근 가능
        if (capsule.getReceiver().getId().equals(userId)) {
            return !LocalDateTime.now().isBefore(capsule.getOpenAt());
        }

        return false;
    }

    /**
     * 사용자가 캡슐과 관련이 있는지 확인 (보낸 사람 또는 받는 사람)
     */
    private boolean isUserRelatedToCapsule(Long userId, TimeCapsule capsule) {
        return capsule.getSender().getId().equals(userId) || capsule.getReceiver().getId().equals(userId);
    }
}

