package com.sparta.omin.app.model.admin.repository;

import com.sparta.omin.app.model.admin.entity.Admin;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, UUID> {

	boolean existsByEmail(String email);

	Optional<Admin> findByEmailAndIsDeletedFalse(String email);

}
