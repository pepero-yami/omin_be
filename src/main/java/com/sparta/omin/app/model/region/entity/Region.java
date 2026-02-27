package com.sparta.omin.app.model.region.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_region")
public class Region {

    @Getter
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Getter
    @Column(name = "address", length = 100, nullable = false)
    private String address;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Getter
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    protected Region() {}

    public static Region create(UUID id, String address, UUID actorId, LocalDateTime now) {
        Region region = new Region();
        region.id = id;
        region.address = address;
        region.createdAt = now;
        region.createdBy = actorId;
        region.updatedAt = now;
        region.updatedBy = actorId;
        region.isDeleted = false;
        return region;
    }

    public void updateAddress(String newAddress, UUID actorId, LocalDateTime now) {
        this.address = newAddress;
        this.updatedAt = now;
        this.updatedBy = actorId;
    }

    public void softDelete(UUID actorId, LocalDateTime now) {
        this.isDeleted = true;
        this.deletedAt = now;
        this.deletedBy = actorId;
        this.updatedAt = now;
        this.updatedBy = actorId;
    }

}