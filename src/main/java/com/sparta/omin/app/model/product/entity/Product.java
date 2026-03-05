package com.sparta.omin.app.model.product.entity;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="p_product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name="id", updatable = false, nullable = false)
    private UUID id;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="description")
    private String description;

    @Column(name="price", nullable = false)
    private Double price;

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status =  ProductStatus.ON_SALE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder
    public Product (String name, String description, Double price, ProductStatus status, Store store) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = (status != null) ? status : ProductStatus.ON_SALE;
        this.store = store;
    }
}
