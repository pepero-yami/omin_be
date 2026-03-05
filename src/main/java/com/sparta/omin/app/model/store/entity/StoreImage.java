package com.sparta.omin.app.model.store.entity;

import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_store_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreImage extends BaseEntity {
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name="is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    public void setStore(Store store) {
        this.store = store;
    }
    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public StoreImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
