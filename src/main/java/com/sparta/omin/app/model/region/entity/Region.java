package com.sparta.omin.app.model.region.entity;

import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_region")
public class Region extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "address", length = 100, nullable = false)
    private String address;

    public static Region create(String address) {
        Region region = new Region();
        region.address = address;
        region.isDeleted = false;
        return region;
    }

    public void updateAddress(String newAddress) {
        this.address = newAddress;
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}