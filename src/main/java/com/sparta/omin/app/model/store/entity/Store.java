package com.sparta.omin.app.model.store.entity;

import com.sparta.omin.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.sparta.omin.app.model.user.entity.User;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "update p_store set is_deleted = true where id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "p_store")
public class Store extends BaseTimeEntity {
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;                                                    //도로명주소

    @Column(name = "detail_address", nullable = false, length = 100)
    private String detailAddress;                                                  //상세주소

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;                                                    //위도

    @Column(name = "longitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;                                                   //경도

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "updated_by", updatable = false)
    private UUID updatedBy;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<StoreImage> images = new ArrayList<>();

    public void addImage(StoreImage storeImage) {
        images.add(storeImage);
        storeImage.setStore(this);
        storeImage.setSequence(images.size());
    }

    public void updateStore(UUID regionId, Category category, String name, String roadAddress, String detailAddress, BigDecimal latitude, BigDecimal longitude) {
        this.regionId = regionId;
        this.category = category;
        this.name = name;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    @Builder
    public Store(UUID ownerId, UUID regionId, Category category, String name, String roadAddress, String detailAddress, BigDecimal latitude, BigDecimal longitude) {
        this.ownerId = ownerId;
        this.regionId = regionId;
        this.category = category;
        this.name = name;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @PrePersist
    public void onPrePersist() {
        UUID currentUserId = getCurrentUserId();
        this.createdBy = currentUserId;
        this.updatedBy = currentUserId;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedBy = getCurrentUserId();
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            Object principal = auth.getPrincipal();

            if (principal instanceof User) {
                User user = (User) principal;

                // 1. user.getId()가 이미 UUID 타입을 반환하는 경우:
                return user.getId();

                // 2. 만약 user.getId()가 String 타입을 반환한다면 아래처럼 변환해주세요:
                // return UUID.fromString(user.getId());
            }
        }

        // 비회원 또는 시스템 처리 로직 (둘 중 프로젝트에 맞는 방식을 선택하세요)
        return null; // 방식 A: null 허용 컬럼인 경우
        // return UUID.fromString("00000000-0000-0000-0000-000000000000"); // 방식 B: null 불가 시 시스템용 기본 UUID
    }
}
