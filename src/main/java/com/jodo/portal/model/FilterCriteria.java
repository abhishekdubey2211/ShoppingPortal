package com.jodo.portal.model;

public class FilterCriteria {
    private String fieldName;
    private String operation;
    private Object value;
    private Object additionalValue; 
    private String conditionType;

    public FilterCriteria() {
    }

    public FilterCriteria(String fieldName, String operation, Object value, String conditionType) {
        this.fieldName = fieldName;
        this.operation = operation;
        this.value = value;
        this.conditionType = conditionType;
    }

    public FilterCriteria(String fieldName, String operation, Object value, Object additionalValue, String conditionType) {
        this.fieldName = fieldName;
        this.operation = operation;
        this.value = value;
        this.additionalValue = additionalValue;
        this.conditionType = conditionType;
    }

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getAdditionalValue() {
		return additionalValue;
	}

	public void setAdditionalValue(Object additionalValue) {
		this.additionalValue = additionalValue;
	}

	public String getConditionType() {
		return conditionType;
	}

	public void setConditionType(String conditionType) {
		this.conditionType = conditionType;
	}
    
    

}
