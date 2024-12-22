package com.jodo.portal.exceptions;

public class JwtValidationException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int statusCode;
	private final String statusDescription;

	public JwtValidationException(int statusCode, String statusDescription, String message) {
		super(message);
		this.statusCode = statusCode;
		this.statusDescription = statusDescription;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusDescription() {
		return statusDescription;
	}
}
