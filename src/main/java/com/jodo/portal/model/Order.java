package com.jodo.portal.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "user_order",
       indexes = {
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_order_date", columnList = "order_date"),
           @Index(name = "idx_status", columnList = "status")
       })
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private EndUser user;

    @Column(name = "order_date")
    private String date;

    @Column(name = "total_amount")
    private double totalamount;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Bucket> bucket;

    @Column(name = "paymentmode")
    private String paymentmode;

    @Column(name = "ispaymentdone")
    private int ispaymentdone;

    @Column(name = "status")
    private String status;

    @Column(name = "isdelevered")
    private String isdelevered;
}
