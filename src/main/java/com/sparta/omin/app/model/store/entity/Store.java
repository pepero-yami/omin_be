package com.sparta.omin.app.model.store.entity;

import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "update p_store set is_deleted = true where id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "p_store")
public class Store extends BaseEntity {
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "detail_address", nullable = false, length = 100)
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

//    @Column(name = "coordinates", columnDefinition = "geography(Point, 4326)", nullable = false)
//    private Point coordinates;

    private Point coordinates;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreImage> images = new ArrayList<>();

    public void addImage(StoreImage storeImage) {
        images.add(storeImage);
        storeImage.mappingNewStoreImage(images.size(), this);
    }

    public void addNewImage(String imageUrl, int sequence) {
        StoreImage storeImage = new StoreImage(imageUrl);
        storeImage.mappingNewStoreImage(sequence, this);
        images.add(storeImage);
    }

    public void removeImagesIn(Set<UUID> deleteIds) {
        images.removeIf(img -> deleteIds.contains(img.getId()));
    }

    public void updateStore(Category category, String name, String roadAddress, String detailAddress, Point coordinates) {
        this.category = category;
        this.name = name;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.coordinates = coordinates;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    @Builder
    public Store(UUID ownerId, Category category, String name, String roadAddress, String detailAddress, Point coordinates) {
        this.ownerId = ownerId;
        this.category = category;
        this.name = name;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.coordinates = coordinates;
    }
}
