package com.project.deartime.app.gallery.dto.photos;

public record PhotoUploadRequest (
        String caption,
        Long albumId
){
}
