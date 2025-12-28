package com.project.deartime.app.gallery.dto.photos;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record PhotoCaptionUpdateRequest(
        @NotNull(message = "캡션 내용은 필수입니다.")
        @Size(max = 500, message = "캡션은 500자 이하로 작성해야 합니다.")
        String caption
) {}
