package com.sparta.omin.app.model.user.repository;

import com.sparta.omin.app.model.user.constants.Role;
import com.sparta.omin.app.model.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

	boolean existsByEmail(String email);

	Optional<User> findByEmailAndIsDeletedFalse(String email);

	Optional<User> findByIdAndIsDeletedFalse(UUID id);
}