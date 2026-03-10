package com.sparta.omin.app.model.product.dto;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public record ProductImageUpdateCommand(
    UUID imageId,
    MultipartFile file,
    int sortOrder,
    boolean isPrimary
) {
    public boolean isNewImage() {
        return file != null;
    }

    public boolean isExistingImage() {
        return imageId != null;
    }
}