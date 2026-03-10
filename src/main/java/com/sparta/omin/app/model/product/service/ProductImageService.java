package com.sparta.omin.app.model.product.service;

import com.sparta.omin.app.model.product.dto.ProductImageResult;
import com.sparta.omin.app.model.product.dto.ProductImageUpdateCommand;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.entity.ProductImage;
import com.sparta.omin.app.model.product.repos.ProductImageRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import com.sparta.omin.common.util.ImageUploader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    // 메뉴별 저장할 수 있는 최대 이미지 개수
    private static final int MAX_IMAGE_COUNT = 5;

    private ImageUploader imageUploader;
    private ProductImageRepository productImageRepository;

    /**
     * 상품 생성시, 사진이 있는 경우 사진을 저장합니다.
     * @param product 추가된 상품 객체
     * @param files 상품의 이미지(들)
     */
    @Transactional
    public void createImages(Product product, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // 혹시나 front에서 사진 갯수를 잘 못보낼 경우 예외 처리
        if (files.size() > MAX_IMAGE_COUNT) {
            throw new OminBusinessException(ErrorCode.PRODUCT_INVALID_IMAGE_COUNT);
        }

        // front에서 사용자가 설정한 사진 순서대로 요청을 보낸다는 가정 하에,
        // 0번 index에 있는 사진을 대표사진으로 간주합니다.
        List<ProductImage> images = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            String imgUrl = imageUploader.uploadReviewImage(files.get(i));

            images.add(ProductImage.builder()
                .url(imgUrl)
                .isPrimary(i == 0)
                .sortOrder(i)
                .product(product)
                .build());
        }

        productImageRepository.saveAll(images);
    }

    /**
     * @param productId
     * @return s3에 저장된 이미지의 url을 {@code List<String>}으로 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<ProductImageResult> getImages(UUID productId) {
        return productImageRepository.findAllByProductIdAndIsDeletedFalseOrderBySortOrderAsc(productId)
            .stream().map(ProductImageResult::from).toList();
    }

    /**
     * <b>상품 이미지를 수정</b>합니다.<br>
     *상품 이미지 수정은 아래의 순서로 처리됩니다.<br>
     * 1. 기존 이미지 조회<br>
     * 2. 요청내의 imageId가 목표 상품의 이미지인지 검증<br>
     * 3. Map 변환<br>
     * 4. 삭제 대상 delete<br>
     * 5. 메타 데이터 수정<br>
     * 6. 새 이미지 생성
     * @param product 이미지를 수정하려는 상품의 {@code UUID}
     * @param commands
     */
    @Transactional
    public void updateImages(
        Product product,
        List<ProductImageUpdateCommand> commands
    ) {
        // 변경사항 이미지 개수 검증
        if (commands.size() > MAX_IMAGE_COUNT) {
            throw new OminBusinessException(ErrorCode.PRODUCT_INVALID_IMAGE_COUNT);
        }

        // 기존 이미지 조회
        List<ProductImage> existingImages = productImageRepository
            .findAllByProductIdAndIsDeletedFalse(product.getId());

        // 이미지 검증
        validateExistingImageOwnership(existingImages, commands);

        // imageId로 빠르게 찾기위한 Map변환
        Map<UUID, ProductImage> existingImageMap = existingImages.stream()
            .collect(Collectors.toMap(ProductImage::getId, Function.identity()));

        // 삭제 대상 처리
        softDeleteImages(existingImages, commands);

        // 메타 데이터 수정
        updateExistingImages(existingImageMap, commands);

        // 새 이미지 생성
        createNewImages(product, commands);
    }

    private void updateExistingImages(
        Map<UUID, ProductImage> existingImageMap,
        List<ProductImageUpdateCommand> imageCommands
    ) {
        for (ProductImageUpdateCommand command : imageCommands) {
            if (!command.isExistingImage()) {
                continue;
            }

            ProductImage image = existingImageMap.get(command.imageId());
            image.updateMetadata(
                command.sortOrder(),
                command.isPrimary()
            );
        }
    }

    private void createNewImages(
        Product product,
        List<ProductImageUpdateCommand> imageCommands
    ) {
        List<ProductImage> newImages = new ArrayList<>();

        for (ProductImageUpdateCommand command : imageCommands) {
            if (!command.isNewImage()) {
                continue;
            }

            String imageUrl = imageUploader.uploadReviewImage(command.file());

            newImages.add(ProductImage.builder()
                .product(product)
                .url(imageUrl)
                .sortOrder(command.sortOrder())
                .isPrimary(command.isPrimary())
                .build());
        }

        productImageRepository.saveAll(newImages);
    }

    private void validateExistingImageOwnership(
        List<ProductImage> existingImages,
        List<ProductImageUpdateCommand> imageCommands
    ) {
        Set<UUID> existingImageIds = existingImages.stream()
            .map(ProductImage::getId)
            .collect(Collectors.toSet());

        for (ProductImageUpdateCommand command : imageCommands) {
            if (command.isExistingImage() && !existingImageIds.contains(command.imageId())) {
                throw new OminBusinessException(ErrorCode.PRODUCT_IMAGE_INVALID_ACCESS);
            }
        }
    }

    /**
     * 상품의 사진을 전체 제거(soft delete)합니다.
     * 상품을 삭제할때만 사용합니다.
     * @param productId 삭제하려는 상품의 id입니다
     */
    @Transactional
    public void deleteAllProductImages(UUID productId) {
        List<ProductImage> images = productImageRepository.findAllByProductIdAndIsDeletedFalse(productId);
        images.forEach(ProductImage::softDelete);
    }

    private void softDeleteImages(List<ProductImage> existingImages, List<ProductImageUpdateCommand> commands) {
        Set<UUID> requestedExistingImageIds = commands.stream()
            .filter(ProductImageUpdateCommand::isExistingImage)
            .map(ProductImageUpdateCommand::imageId)
            .collect(Collectors.toSet());

        for (ProductImage existingImage : existingImages) {
            if (!requestedExistingImageIds.contains(existingImage.getId())) {
                existingImage.softDelete();
            }
        }
    }
}
