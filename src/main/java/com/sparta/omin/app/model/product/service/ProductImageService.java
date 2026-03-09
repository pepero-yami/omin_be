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
    public void create(Product product, MultipartFile image) {
        if(image != null && !image.isEmpty()) {
            String imgUrl = imageUploader.uploadReviewImage(image);
            productImageRepository.save(ProductImage.builder()
                .url(imgUrl)
                .product(product)
                .build()
            );
        }
    }

    /**
     * @param productId
     * @return s3에 저장된 이미지의 url을 {@code List<String>}으로 반환합니다.
     */
    public List<String> getImgUrl(UUID productId) {
        return productImageRepository.findByIdAndIsDeletedFalse(productId)
            .stream()
            .map(ProductImage::getUrl)
            .toList();
    }
}
