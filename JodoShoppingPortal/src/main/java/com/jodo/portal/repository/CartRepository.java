package com.jodo.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jodo.portal.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long>{

}
