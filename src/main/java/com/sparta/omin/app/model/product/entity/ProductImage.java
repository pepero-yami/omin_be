package com.sparta.omin.app.model.product.entity;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_product_image")
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name="id", updatable = false, nullable = false)
    private UUID id;

    @Column(name="url", nullable = false)
    private String url;

    @Column(name="is_primary", nullable = false)
    private Boolean isPrimary;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ---------------------
    // Methods
    // ---------------------
    @Builder
    public ProductImage(ProductStatus productStatus, String url, Product product, Integer sortOrder, Boolean isPrimary) {
        this.url = url;
        this.isPrimary = isPrimary;
        this.sortOrder = sortOrder;
        this.product = product;
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}
