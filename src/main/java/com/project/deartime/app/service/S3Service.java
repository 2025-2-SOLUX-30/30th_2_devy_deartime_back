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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 업로드
     */
    public String uploadFile(MultipartFile file, String folder) {
        String fileName = createFileName(file.getOriginalFilename(), folder);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }

        return amazonS3.getUrl(bucket, fileName).toString();
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
     * 파일 확장자 추출 (수정됨)
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