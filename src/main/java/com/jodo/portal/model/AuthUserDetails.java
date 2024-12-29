package com.jodo.portal.model;

import java.util.Set;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AuthUserDetails {
	private Long userId;
	private Long cartId;
	private String loginId;
	private String sessionId;
	private String roles;
	private String logindatetime;
}