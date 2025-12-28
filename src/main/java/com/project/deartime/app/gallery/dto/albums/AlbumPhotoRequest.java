package com.project.deartime.app.gallery.dto.albums;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AlbumPhotoRequest(
        @NotNull(message = "사진 ID 목록은 필수입니다.")
        @NotEmpty(message = "추가할 사진 ID가 최소 하나 필요합니다.")
        List<Long> photoIds
) {}
