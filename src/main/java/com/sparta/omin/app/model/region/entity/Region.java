package com.sparta.omin.app.model.region.entity;

import com.sparta.omin.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_region")
public class Region extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private java.util.UUID id;

    @Column(name = "address", length = 100, nullable = false)
    private String address;

    public static Region create(String address, java.util.UUID actorId) {
        Region region = new Region();
        region.address = address;

        region.createdBy = actorId;
        region.updatedBy = actorId;
        region.isDeleted = false;

        return region;
    }

    public void updateAddress(String newAddress, java.util.UUID actorId) {
        this.address = newAddress;
        this.updatedBy = actorId;
    }

    public void softDelete(java.util.UUID actorId) {
        this.isDeleted = true;
        this.updatedBy = actorId;
    }
}