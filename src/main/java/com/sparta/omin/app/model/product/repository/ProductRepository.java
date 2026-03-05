package com.sparta.omin.app.model.product.repository;

import com.sparta.omin.app.model.product.entity.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

}
