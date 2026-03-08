package com.sparta.omin.app.model.product.repos;

import com.sparta.omin.app.model.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
	Optional<Product> findByIdAndStoreId(UUID id, UUID storeId);

    List<Product> findByIdInAndStoreId(List<UUID> productIds, UUID storeId);
}
