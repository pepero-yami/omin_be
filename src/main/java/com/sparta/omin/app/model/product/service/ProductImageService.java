package com.sparta.omin.app.model.product.service;

import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.entity.ProductImage;
import com.sparta.omin.app.model.product.repos.ProductImageRepository;
import com.sparta.omin.common.util.ImageUploader;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageService {

    private ImageUploader imageUploader;
    private ProductImageRepository productImageRepository;

    /**
     * 상품 생성시, 사진이 있는 경우 사진을 저장합니다.
     * @param product
     * @param image
     */
    @Transactional
    public void createImages(Product product, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<ProductImage> images = files.stream()
            .map(imageUploader::uploadReviewImage)
            .map(imgUrl -> ProductImage.builder()
                .url(imgUrl)
                .product(product)
                .build())
            .toList();

        productImageRepository.saveAll(images);
    }

    /**
     * @param productId
     * @return s3에 저장된 이미지의 url을 {@code List<String>}으로 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<String> getImgUrl(UUID productId) {
        return productImageRepository.findByProductIdAndIsDeletedFalse(productId)
            .stream()
            .map(ProductImage::getUrl)
            .toList();
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
