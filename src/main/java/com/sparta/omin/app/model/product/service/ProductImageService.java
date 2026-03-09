package com.sparta.omin.app.model.product.service;

import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.entity.ProductImage;
import com.sparta.omin.app.model.product.repos.ProductImageRepository;
import com.sparta.omin.common.util.ImageUploader;
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
}
