package com.project.deartime.app.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

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
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
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
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
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
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
    }
}