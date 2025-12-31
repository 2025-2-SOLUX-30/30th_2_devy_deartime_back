package com.project.deartime.app.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.project.deartime.global.exception.CoreApiException;
import com.project.deartime.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /** 허용 이미지 MIME 타입 */
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png"
    );

    /** 최대 파일 크기 (10MB) */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 파일 업로드
     */
    public String uploadFile(MultipartFile file, String folder) {
        validateImageFile(file);

        String fileName = createFileName(file.getOriginalFilename(), folder);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(
                    new PutObjectRequest(bucket, fileName, inputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );
            log.info("[S3 UPLOAD] 파일 업로드 성공. fileName={}", fileName);
        } catch (Exception e) {
            log.error("[S3 UPLOAD] 파일 업로드 실패. fileName={}", fileName, e);
            throw new CoreApiException(
                    ErrorCode.S3_FILE_UPLOAD_FAILED,
                    e
            );
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    /**
     * 이미지 파일 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 최대 10MB까지 업로드할 수 있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("jpg, jpeg, png 형식의 이미지만 업로드할 수 있습니다.");
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileUrl) {
        String fileName = extractFileNameFromUrl(fileUrl);

        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
            log.info("[S3 DELETE] 파일 제거 성공. fileName={}", fileName);
        } catch (Exception e) {
            log.error(
                    "[S3 DELETE] 파일 제거 실패. fileName={}, bucket={}",
                    fileName,
                    bucket,
                    e
            );
            throw new CoreApiException(
                    ErrorCode.S3_FILE_DELETE_FAILED,
                    e
            );
        }
    }

    /**
     * 고유한 파일명 생성
     */
    private String createFileName(String originalFileName, String folder) {
        String ext = extractExt(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return folder + "/" + uuid + "." + ext;
    }

    /**
     * 파일 확장자 추출
     */
    private String extractExt(String originalFileName) {
        int pos = originalFileName.lastIndexOf(".");
        if (pos == -1) {
            return "jpg"; // 기본 확장자
        }
        return originalFileName.substring(pos + 1);
    }

    /**
     * URL에서 파일명(S3 Object Key) 추출
     * Virtual Hosted-Style URL 지원: https://{bucket}.s3.{region}.amazonaws.com/{key}
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            log.error("[S3 URL] fileUrl is null or empty");
            throw new CoreApiException(ErrorCode.INVALID_S3_FILE_URL);
        }

        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();

            if (path == null || path.length() <= 1) {
                log.error("[S3 URL] Invalid path in URL. fileUrl={}", fileUrl);
                throw new CoreApiException(ErrorCode.INVALID_S3_FILE_URL);
            }

            // 맨 앞의 '/' 제거하여 S3 Object Key 추출
            String key = path.startsWith("/") ? path.substring(1) : path;

            // URL 디코딩 (한글 파일명 등 처리)
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);

            log.debug("[S3 URL] Extracted key: {} from URL: {}", key, fileUrl);
            return key;
        } catch (URISyntaxException e) {
            log.error("[S3 URL] Failed to parse URL. fileUrl={}", fileUrl, e);
            throw new CoreApiException(ErrorCode.INVALID_S3_FILE_URL);
        }
    }
}