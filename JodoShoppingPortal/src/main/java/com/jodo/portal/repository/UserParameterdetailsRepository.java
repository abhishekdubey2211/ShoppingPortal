package com.jodo.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.UserParameterDetails;
import jakarta.transaction.Transactional;
import java.util.*;
public interface UserParameterdetailsRepository extends JpaRepository<UserParameterDetails,Long>{

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_parameterdetails WHERE end_user_id = ?", nativeQuery = true)
    void deleteUserParameterDetailsByUserid(@Param("userId") Long userId);
    
	public List<UserParameterDetails> findByUser(EndUser user);
    public boolean deleteByUser(EndUser user);

}
