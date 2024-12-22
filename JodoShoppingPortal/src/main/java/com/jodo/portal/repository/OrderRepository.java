package com.jodo.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByUser(EndUser user);
}
