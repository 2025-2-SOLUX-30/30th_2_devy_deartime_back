package com.project.deartime.app.dto.photos;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PhotoUploadRequest (
        List<MultipartFile> files,
        String caption,
        Long albumId
){
}
