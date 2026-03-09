package com.sparta.omin.app.model.product.repos;

import com.sparta.omin.app.model.product.dto.ProductWithUrlResult;
import com.sparta.omin.app.model.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndIsDeletedFalse(UUID productId);

    List<Product> findByStoreIdAndIsDeletedFalse(UUID storeId);
	Optional<Product> findByIdAndStoreId(UUID id, UUID storeId);

    List<Product> findByIdInAndStoreId(List<UUID> productIds, UUID storeId);

    @Query("""
      select new com.sparta.omin.app.model.product.dto.ProductWithUrlResult(
            p.id,
            p.store.id,
            p.name,
            p.description,
            p.price,
            p.status,
            pi.url
          )
          from Product p
          left join ProductImage pi
              on pi.product.id = p.id
              and pi.isDeleted = false
          where p.store.id = :storeId
              and p.isDeleted = false
    """)
    List<ProductWithUrlResult> findProductListWithUrl(UUID storeId);
}
