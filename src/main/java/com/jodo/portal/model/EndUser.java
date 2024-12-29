package com.jodo.portal.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "user", indexes = { @Index(name = "idx_username", columnList = "username"),
		@Index(name = "idx_email", columnList = "email"), @Index(name = "idx_contact", columnList = "contact") })
public class EndUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotBlank(message = "Username is mandatory")
	@Size(max = 50, message = "Username must be less than 50 characters")
	private String username;

	@NotBlank(message = "Password is mandatory")
	private String password;

	@NotBlank(message = "Designation is mandatory")
	@Size(max = 100, message = "Designation must be less than 100 characters")
	private String designation;

	@NotBlank(message = "Email is mandatory")
	@Email(message = "Invalid Email Address")
	@Column(unique = true)
	private String email;

	@NotBlank(message = "Contact number is mandatory")
	@Pattern(regexp = "^\\+?[0-9]{1,3}?[-.\\s]?\\(?[0-9]{1,4}?\\)?[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,9}$", message = "Invalid contact number format")
	private String contact;

	private String profileimage;

	private int isactive; // Consider changing to boolean if appropriate

	private int isdelete;

	private String usercreationdate;

	@Column(name = "date_of_birth")
	private String dateofbirth;

	private String userlastupdatedate;

	@Transient
	private String role;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "role")
	private Set<Role> roles = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<UserParameterDetails> userParameterDetails = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<RessidentialAddress> address = new ArrayList<>();

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "cart_id")
	@JsonIgnore
	private Cart cart;
}
