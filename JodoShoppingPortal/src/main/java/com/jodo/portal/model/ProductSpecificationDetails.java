package com.jodo.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jodo.portal.dto.ProductSpecificationDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table(name="product_specifications")
public class ProductSpecificationDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore  
    private long id;
    
    @Column(name="fieldid")
    private int fieldid;
    
    @Column(name="srno")
    private int srno;
    
    @Column(name="fieldvalue")
    private String fieldvalue;
    
    @Column(name="fieldname")
    private String fieldname;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="product_id", referencedColumnName = "id")
    @JsonIgnore
    private Product product;
}
