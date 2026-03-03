package com.sparta.omin.app.model.product.entity;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@NoArgsConstructor
@Table(name="p_product")
public class Product extends BaseTimeEntity {

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
    private ProductStatus status =  ProductStatus.ON_SALE;

    // TODO : Store Entity 작성 완료시 활성화 시킬 것.
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id", nullable = false)
//    private Store store;
}
