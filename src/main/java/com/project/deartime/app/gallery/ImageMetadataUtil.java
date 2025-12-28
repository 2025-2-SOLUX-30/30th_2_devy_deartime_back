package com.project.deartime.app.gallery;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ImageMetadataUtil {

    public static LocalDateTime extractTakenAt(byte[] imageBytes, String fileName) {
        // 1. 먼저 메타데이터 추출 시도
        LocalDateTime takenAt = extractFromMetadata(imageBytes);
        if (takenAt != null) return takenAt;

        // 2. 메타데이터가 없으면 파일명 분석 (KakaoTalk_20250410_... 형식)
        return extractFromFileName(fileName);
    }

    private static LocalDateTime extractFromMetadata(byte[] imageBytes) {
        try (InputStream is = new ByteArrayInputStream(imageBytes)) {
            Metadata metadata = ImageMetadataReader.readMetadata(is);
            ExifSubIFDDirectory subIfd = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (subIfd != null) {
                String dateStr = subIfd.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (dateStr != null) {
                    return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"));
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static LocalDateTime extractFromFileName(String fileName) {
        if (fileName == null) return null;

        try {
            // 카카오톡 파일명 예시: KakaoTalk_20250410_215409275.jpg
            if (fileName.contains("KakaoTalk_")) {
                String datePart = fileName.split("_")[1]; // "20250410"
                int year = Integer.parseInt(datePart.substring(0, 4));
                int month = Integer.parseInt(datePart.substring(4, 6));
                int day = Integer.parseInt(datePart.substring(6, 8));
                return LocalDateTime.of(year, month, day, 0, 0);
            }
        } catch (Exception e) {
            System.err.println("파일명에서 날짜 추출 실패: " + fileName);
        }
        return null;
    }
}