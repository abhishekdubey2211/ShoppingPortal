package com.jodo.portal.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Data;

//@Data
public class CustomException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int statusCode;
	private final String statusDescription;
	private final HttpStatus status;

	public CustomException(int statusCode, String statusDescription, String message) {
		super(message);
		this.statusCode = statusCode;
		this.statusDescription = statusDescription;
		this.status = null;
	}

	public CustomException(int statusCode, String statusDescription, String message,HttpStatus status) {
		super(message);
		this.statusCode = statusCode;
		this.statusDescription = statusDescription;
		this.status = status;
	}

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public HttpStatus getStatus() {
        return status;
    }
	
	
}
