package com.sparta.omin.app.model.order.entity;

import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor
// 임시 Stub입니다. 이 파일을 무시해주세요.
public class Order extends BaseAuditEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    private UUID userId;
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    @ManyToOne
    private Store store;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public boolean isCompleted() {
        return this.status == OrderStatus.COMPLETED;
    }

    private enum OrderStatus {
        COMPLETED
    }
}