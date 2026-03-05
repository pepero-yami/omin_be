package com.sparta.omin.app.model.product.entity;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@NoArgsConstructor
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
    private double price;

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status =  ProductStatus.ON_SALE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
}
