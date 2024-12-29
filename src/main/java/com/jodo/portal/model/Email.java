package com.jodo.portal.model;

public class Email {

	private int id;
	private String organisationName;
	private String fromAddress;
	private String toAddresses;
	private String bccAddresses;
	private String ccAddresses;
	private String subject;
	private String body;
	private boolean fileAttached;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOrganisationName() {
		return organisationName;
	}

	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getToAddresses() {
		return toAddresses;
	}

	public void setToAddresses(String toAddresses) {
		this.toAddresses = toAddresses;
	}

	public String getBccAddresses() {
		return bccAddresses;
	}

	public void setBccAddresses(String bccAddresses) {
		this.bccAddresses = bccAddresses;
	}

	public String getCcAddresses() {
		return ccAddresses;
	}

	public void setCcAddresses(String ccAddresses) {
		this.ccAddresses = ccAddresses;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isFileAttached() {
		return fileAttached;
	}

	public void setFileAttached(boolean fileAttached) {
		this.fileAttached = fileAttached;
	}

}
