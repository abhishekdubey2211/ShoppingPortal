package com.jodo.portal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
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
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "brand_name")
    private String brand;

    @Column(name = "category_name")
    private String category;

    @Column(name="model_detail")
    private String model;
    
    @Column(name = "discount")
    private double discount;

    @Column(name ="marketprice")
    private double marketprice;
    
    @Column(name = "price")
    private double price;

    @Column(name = "quantity")
    private int quantity;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ProductImages> images;

    @Column(name = "description")
    private String description;

    @Column(name = "productstatus")
    private String productstatus;

    @Column(name = "instock")
    private int instock;

    @Column(name = "active")
    private int active;

    @Column(name = "isdelete")
    private int isdelete;

    @Column(name = "last_recieved_date")
    private String last_refilled_date;
    
    @Column(name = "last_updated_date")
    @JsonIgnore    
    private String lastupdateddate;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore    
    private List<ProductSpecificationDetails> productspecification;
    
    // Uncomment if needed
    // @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    // @JsonIgnore
    // List<CartItem> cartItem;
}
