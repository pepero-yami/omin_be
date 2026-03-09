package com.sparta.omin.app.model.product.service;

import com.sparta.omin.app.model.product.dto.ProductImageResult;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.entity.ProductImage;
import com.sparta.omin.app.model.product.repos.ProductImageRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import com.sparta.omin.common.util.ImageUploader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageService {

    // 메뉴별 저장할 수 있는 최대 이미지 개수
    private static final int MAX_IMAGE_COUNT = 5;

    private ImageUploader imageUploader;
    private ProductImageRepository productImageRepository;

    /**
     * 상품 생성시, 사진이 있는 경우 사진을 저장합니다.
     * @param product 추가된 상품 객체
     * @param files 상품의 이미지(들)
     * @param primaryImageIndex 대표 이미지 위치
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
     * 상품의 사진(들)을 수정합니다.<br>
     * 수정은 기존 사진을 제거하거나, 새로운 사진을 추가함을 의미합니다.
     * @param product
     * @param imgIds
     * @param files
     */
    @Transactional
    public void updateImages(Product product, List<UUID> imgIds, List<MultipartFile> files) {
        deleteImages(imgIds);
        createImages(product, files);
    }

    /**
     * 상품의 사진을 제거(soft delete)합니다.
     * @param imgIds 삭제하려는 이미지(들)의 id입니다
     */
    @Transactional
    public void deleteImages(List<UUID> imgIds) {
        if (imgIds == null || imgIds.isEmpty()) {
            return;
        }
        List<ProductImage> images = productImageRepository.findAllByIdInAndIsDeletedFalse(imgIds);
        images.forEach(ProductImage::softDelete);
    }
}
