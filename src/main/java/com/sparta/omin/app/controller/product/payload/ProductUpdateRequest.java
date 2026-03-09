package com.sparta.omin.app.controller.product.payload;

import com.sparta.omin.app.model.product.dto.ProductImageUpdateCommand;
import com.sparta.omin.app.model.product.dto.ProductUpdateCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public record ProductUpdateRequest(
    @NotEmpty
    String name,

    String description,

    @NotNull
    Double price,

    @NotEmpty
    @Valid
    List<ProductImageUpdateItemRequest> items
) {

    public ProductUpdateCommand toCommand(List<MultipartFile> files) {
        List<MultipartFile> safeFiles = files == null ? List.of() : files;

        validateItems(safeFiles);

        List<ProductImageUpdateCommand> imageCommands = items.stream()
            .map(item -> item.toCommand(safeFiles))
            .toList();

        return new ProductUpdateCommand(
            name,
            description,
            price,
            imageCommands
        );
    }

    private void validateItems(List<MultipartFile> files) {
        validateImageIdentity();
        validateSortOrder();
        validatePrimaryImage();
        validateNewImageIndex(files);
    }

    private void validateImageIdentity() {
        for (ProductImageUpdateItemRequest item : items) {
            boolean hasImageId = item.imageId() != null;
            boolean hasNewImageIndex = item.newImageIndex() != null;

            if (hasImageId == hasNewImageIndex) {
                throw new IllegalArgumentException("imageId 또는 newImageIndex 중 하나만 지정해야 합니다.");
            }
        }
    }

    private void validateSortOrder() {
        Set<Integer> sortOrders = new HashSet<>();

        for (ProductImageUpdateItemRequest item : items) {
            if (!sortOrders.add(item.sortOrder())) {
                throw new IllegalArgumentException("sortOrder는 중복될 수 없습니다.");
            }
        }
    }

    private void validatePrimaryImage() {
        long primaryCount = items.stream()
            .filter(ProductImageUpdateItemRequest::isPrimary)
            .count();

        if (primaryCount != 1) {
            throw new IllegalArgumentException("대표 이미지는 반드시 1개여야 합니다.");
        }
    }

    private void validateNewImageIndex(List<MultipartFile> files) {
        for (ProductImageUpdateItemRequest item : items) {
            Integer newImageIndex = item.newImageIndex();

            if (newImageIndex == null) {
                continue;
            }

            if (newImageIndex < 0 || newImageIndex >= files.size()) {
                throw new IllegalArgumentException("유효하지 않은 newImageIndex 입니다.");
            }
        }
    }

    public record ProductImageUpdateItemRequest(
        // 기존 이미지면 존재
        UUID imageId,

        // 새로운 이미지면 존재, files중 몇번째 file인지를 의미
        Integer newImageIndex,

        @NotNull
        Integer sortOrder,

        @NotNull
        Boolean isPrimary
    ) {
        public ProductImageUpdateCommand toCommand(List<MultipartFile> files) {
            MultipartFile file = newImageIndex == null ? null : files.get(newImageIndex);

            return new ProductImageUpdateCommand(
                imageId,
                file,
                sortOrder,
                isPrimary
            );
        }
    }
}