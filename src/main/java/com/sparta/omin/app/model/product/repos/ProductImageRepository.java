package com.sparta.omin.app.model.product.repos;

import com.sparta.omin.app.model.product.entity.ProductImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByIdAndIsDeletedFalse(UUID productId);
}
