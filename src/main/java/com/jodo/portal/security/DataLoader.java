package com.jodo.portal.security;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jodo.portal.dto.EndUserDTO;
import com.jodo.portal.model.Cart;
import com.jodo.portal.model.EndUser;
import com.jodo.portal.model.Role;
import com.jodo.portal.model.UserParameterDetails;
import com.jodo.portal.repository.EnduserRepository;

@Component
public class DataLoader {

	@Autowired
	private EnduserRepository userRepository; // Assuming you have a UserRepository to interact with the database

	@Autowired
	private PasswordEncoder passwordEncoder; // Assuming you're using PasswordEncoder for hashing passwords

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			Optional<String> optionalRole = userRepository.findByRole("SUPERADMIN");
			if (optionalRole.isEmpty()) {
				EndUser defaultSuperAdmin = new EndUser();
				defaultSuperAdmin.setUsername("Abhishek Dubey");
				defaultSuperAdmin.setPassword(passwordEncoder.encode("Abhi@22112000"));
				defaultSuperAdmin.setEmail("abdubey42@gmail.com");
				defaultSuperAdmin.setDesignation("Backend Java Software Developer");
				defaultSuperAdmin.setContact("8850014998");
				defaultSuperAdmin.setIsactive(1);
				defaultSuperAdmin.setIsdelete(0);
				defaultSuperAdmin.setCart(Cart.builder().totalamount(0.0).user(defaultSuperAdmin).build());
				defaultSuperAdmin.setUserParameterDetails(List.of(UserParameterDetails.builder().parameterid(103)
						.srno(1).description("User UniqueId").value(UUID.randomUUID().toString()).user(defaultSuperAdmin).build()));
				defaultSuperAdmin.setRoles(Set.of(Role.SUPERADMIN));
				userRepository.save(defaultSuperAdmin);
				System.out.println("Default SUPERADMIN user created: " + defaultSuperAdmin.getUsername());
			} else {
				System.out.println("Default SUPERADMIN already Exists");
			}
		};
	}
}
