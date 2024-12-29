package com.jodo.portal;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodo.portal.model.FilterCriteria;

@SpringBootTest
class JodoShoppingPortalApplicationTests {

	   @Test
	    public void testDeserialization() throws Exception {
	        String json = "[{\"fieldName\":\"name\",\"operation\":\"like\",\"value\":\"John\",\"conditionType\":\"AND\"}]";

	        ObjectMapper mapper = new ObjectMapper();
	        List<FilterCriteria> criteriaList = Arrays.asList(mapper.readValue(json, FilterCriteria[].class));

	        System.out.println(criteriaList);
	    }
}
