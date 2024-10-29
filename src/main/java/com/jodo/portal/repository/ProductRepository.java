package com.jodo.portal.repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jodo.portal.model.Product;

public interface ProductRepository  extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findAll(Pageable pageable);
    
    Page<Product> findByBrand(String brand, Pageable pageable);

	Page<Product> findAll(Specification<Product> spec, Pageable pageable);
}
