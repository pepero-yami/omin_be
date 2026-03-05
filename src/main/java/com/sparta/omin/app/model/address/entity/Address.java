package com.sparta.omin.app.model.address.entity;

import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_user_address")
public class Address extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Column(name = "shipping_detail_address", nullable = false, length = 100)
    private String shippingDetailAddress;

    @Column(name = "address_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal addressLat;

    @Column(name = "address_long", nullable = false, precision = 10, scale = 7)
    private BigDecimal addressLong;

    @Setter
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "road_address", nullable = false, length = 300)
    private String roadAddress;

    public static Address create(
            UUID userId,
            UUID regionId,
            String nickname,
            String roadAddress,
            String shippingDetailAddress,
            BigDecimal addressLat,
            BigDecimal addressLong,
            boolean isDefault
    ) {
        Address address = new Address();
        address.userId = userId;
        address.regionId = regionId;
        address.nickname = nickname;
        address.roadAddress = roadAddress;
        address.shippingDetailAddress = shippingDetailAddress;
        address.addressLat = addressLat;
        address.addressLong = addressLong;
        address.isDefault = isDefault;
        address.isDeleted = false;
        return address;
    }

    public void update(
            UUID regionId,
            String nickname,
            String roadAddress,
            String shippingDetailAddress,
            BigDecimal addressLat,
            BigDecimal addressLong,
            boolean isDefault
    ) {
        this.regionId = regionId;
        this.nickname = nickname;
        this.roadAddress = roadAddress;
        this.shippingDetailAddress = shippingDetailAddress;
        this.addressLat = addressLat;
        this.addressLong = addressLong;
        this.isDefault = isDefault;
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}