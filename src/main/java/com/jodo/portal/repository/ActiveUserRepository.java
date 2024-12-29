package com.jodo.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jodo.portal.model.ActiveUserDetails;

import jakarta.transaction.Transactional;

public interface ActiveUserRepository extends JpaRepository<ActiveUserDetails, Long>{

	@Transactional
	@Query(value = "SELECT * FROM active_user where sessionid = ?1 And active = ?2", nativeQuery = true)
	Optional<ActiveUserDetails> findBySessionidAndActive(String sessionId,int active);

	Optional<ActiveUserDetails> findByUserid(long id);

	@Transactional
	@Query(value = "SELECT * FROM active_user where userid = ?1 AND active = ?2", nativeQuery = true)
	Optional<ActiveUserDetails> findByUseridAndActive(long userid, int active);
}
