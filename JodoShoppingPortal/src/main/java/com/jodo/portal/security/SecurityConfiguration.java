package com.jodo.portal.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import com.jodo.portal.implementation.EndUserServiceImplementation;

@Component
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

	private final EndUserServiceImplementation endUserService;
	private final AuthTokenFilter authTokenFilter;
	private final AuthEntryPointJWT authEntryPointJWT;

	@Autowired
	public SecurityConfiguration(EndUserServiceImplementation endUserService, AuthTokenFilter authTokenFilter,
			AuthEntryPointJWT authEntryPointJWT) {
		this.endUserService = endUserService;
		this.authTokenFilter = authTokenFilter;
		this.authEntryPointJWT = authEntryPointJWT;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(registry -> {
			registry.requestMatchers("/api/authenticate/**").permitAll();
			registry.requestMatchers(HttpMethod.POST, "/api/v3/user").permitAll();

			registry.requestMatchers("/api/v1/**").hasRole("SUPERADMIN");
			registry.requestMatchers("/api/v2/**").hasAnyRole("SUPERADMIN", "ADMIN");
			registry.requestMatchers("/api/v3/**").hasAnyRole("SUPERADMIN", "ADMIN", "USER");
			registry.requestMatchers("/api/v4/**").hasRole("USER");
			registry.anyRequest().authenticated();
		}).sessionManagement(
				sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPointJWT))
				.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return endUserService;
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(endUserService);
		provider.setPasswordEncoder(passwordEncoder()); // Use default password encoder here
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(authenticationProvider());
	}

	@Bean
	public PasswordEncoder BcryptpasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public PasswordEncoder ArgonpasswordEncoder() {
		int saltLength = 16;
		int hashLength = 32;
		int parallelism = 1;
		int memory = 131072;
		int iterations = 5;
		return new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new AESPasswordEncoder();
	}

	public boolean checkPassword(String rawPassword, String encodedPassword) {
		return passwordEncoder().matches(rawPassword, encodedPassword);
	}
}
