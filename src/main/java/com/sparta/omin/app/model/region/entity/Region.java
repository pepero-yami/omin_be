package com.sparta.omin.app.model.region.entity;

import com.sparta.omin.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.util.UUID;
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
    private UUID id;

    @Column(name = "address", length = 100, nullable = false)
    private String address;

    public static Region create(String address, UUID actorId) {
        Region region = new Region();
        region.address = address;

        region.createdBy = actorId;
        region.updatedBy = actorId;
        region.isDeleted = false;

        return region;
    }

    public void updateAddress(String newAddress, UUID actorId) {
        this.address = newAddress;
        this.updatedBy = actorId;
    }

    public void softDelete(UUID actorId) {
        this.isDeleted = true;
        this.updatedBy = actorId;
    }
}