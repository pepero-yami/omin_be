package com.sparta.omin.app.model.product.repos;

import com.sparta.omin.app.model.product.entity.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndIsDeletedFalse(UUID productId);

    List<Product> findByStoreIdAndIsDeletedFalse(UUID storeId);
}
