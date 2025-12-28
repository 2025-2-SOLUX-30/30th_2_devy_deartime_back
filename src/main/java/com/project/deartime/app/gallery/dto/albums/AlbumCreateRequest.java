package com.project.deartime.app.gallery.dto.albums;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlbumCreateRequest(
        @NotBlank(message = "앨범 제목은 필수입니다.")
        @Size(max = 50, message = "앨범 제목은 50자 이하로 작성해야 합니다.")
        String title,

        Long coverPhotoId
) {}
