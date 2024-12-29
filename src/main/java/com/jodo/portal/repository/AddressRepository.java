package com.jodo.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.RessidentialAddress;
import jakarta.transaction.Transactional;
import java.util.*;
public interface AddressRepository extends JpaRepository<RessidentialAddress,Long>{


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_address WHERE end_user_id = ?", nativeQuery = true)
    void deleteAddressByUserid(@Param("userId") Long userId);
    public List<RessidentialAddress> findByUser(EndUser user);
    public boolean deleteByUser(EndUser user);
}
