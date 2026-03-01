package com.sparta.omin.app.model.region.entity;

import com.sparta.omin.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_region")
public class Region extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "address", length = 100, nullable = false)
    private String address;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public static Region create(UUID id, String address, UUID actorId) {
        Region region = new Region();
        region.id = id;
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

    public void softDelete(UUID actorId, LocalDateTime now) {
        this.isDeleted = true;
        this.deletedAt = now;
        this.deletedBy = actorId;
        this.updatedBy = actorId;
    }
}