package com.jodo.portal.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jodo.portal.model.EndUser;

import jakarta.transaction.Transactional;

@Repository
public interface EnduserRepository extends JpaRepository<EndUser, Long> {
	Optional<EndUser> findByEmail(String email);

	Optional<EndUser> findByUsername(String username);

	@Transactional
	@Query(value = "select role from user_roles where role = ?", nativeQuery = true)
	Optional<String> findByRole(@Param("role") String role);
}
