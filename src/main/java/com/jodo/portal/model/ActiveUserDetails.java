package com.jodo.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Builder
@Table(name = "active_user",
       indexes = {
           @Index(name = "idx_userid", columnList = "userid"),
           @Index(name = "idx_loginid", columnList = "loginid"),
           @Index(name = "idx_sessionid", columnList = "sessionid")
       })
public class ActiveUserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userid;
    private String loginid;
    private String roles;
    private String sessionid;
    private String logindatetime;
    private String logoutdatetime;
    private int active;
    private String logoutreason;
}
