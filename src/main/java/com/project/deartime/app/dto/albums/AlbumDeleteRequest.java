package com.project.deartime.app.dto.albums;

import jakarta.validation.constraints.NotNull;

public record AlbumDeleteRequest(
        @NotNull(message = "사진 영구 삭제 옵션을 선택해야 합니다.")
        Boolean deletePhotos
) {}
