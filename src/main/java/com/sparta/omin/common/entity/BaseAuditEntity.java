package com.sparta.omin.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@MappedSuperclass
// MSA 구조로 확장되어 사용자 정보가 외부에서 내려오는 구조라면 수동이 더 명확하고 안전
// @EntityListeners(AuditingEntityListener.class)
// (TimeEntity) + 작업자 + softDelete = AuditEntity
public abstract class BaseAuditEntity extends BaseTimeEntity {

    @Column(name = "created_by", nullable = false, updatable = false)
    protected UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    protected UUID updatedBy;

    @Column(name = "is_deleted", nullable = false)
    protected boolean isDeleted;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    protected UUID deletedBy;


    protected void markCreated(UUID actorId) {
        this.createdBy = actorId;
        this.updatedBy = actorId;
        this.isDeleted = false;
    }

    protected void markUpdated(UUID actorId) {
        this.updatedBy = actorId;
    }

    protected void markDeleted(UUID actorId, LocalDateTime now) {
        this.isDeleted = true;
        this.deletedAt = now;
        this.deletedBy = actorId;
        this.updatedBy = actorId;
    }
}