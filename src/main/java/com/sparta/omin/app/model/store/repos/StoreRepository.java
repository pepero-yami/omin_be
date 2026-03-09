package com.sparta.omin.app.model.store.repos;

import com.sparta.omin.app.model.store.entity.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByIdAndIsDeletedFalse(UUID storeId);
}
