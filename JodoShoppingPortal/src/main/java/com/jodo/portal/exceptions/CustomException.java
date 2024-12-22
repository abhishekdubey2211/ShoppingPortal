package com.jodo.portal.exceptions;

public class CustomException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int statusCode;
	private final String statusDescription;

	public CustomException(int statusCode, String statusDescription, String message) {
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
