package com.sparta.omin.app.model.store.entity;

import com.sparta.omin.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table (name = "p_store")
public class Store extends BaseTimeEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "category",  nullable = false, columnDefinition = "store_category")
    private Category category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;                                                    //도로명주소

    @Column(name = "detail_address",nullable = false, length = 100)
    private String detailAddress;                                                  //상세주소

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "store_status")
    private Status status = Status.PENDING;

    @Column(name = "latitude", nullable = false, precision = 10,scale = 6)
    private BigDecimal latitude;                                                    //위도

    @Column(name = "longitude", nullable = false, precision = 10,scale = 6)
    private BigDecimal longitude;                                                   //경도

    @Column(name="is_deleted", nullable = false)
    private Boolean isDeleted = false;                                              //삭제여부


    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreImage> images = new ArrayList<>();

    public void addImage(StoreImage storeImage) {
        images.add(storeImage);
        storeImage.setStore(this);
        storeImage.setSequence(images.size());
    }


    @Builder
    public Store(UUID ownerId, UUID regionId, Category category, String name, String roadAddress, String detailAddress, BigDecimal latitude, BigDecimal longitude) {
        this.ownerId = ownerId;
        this.regionId = regionId;
        this.category = category;
        this.name = name;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
