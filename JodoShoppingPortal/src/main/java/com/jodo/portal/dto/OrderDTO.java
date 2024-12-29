package com.jodo.portal.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDTO {
	private long id;
	private String date;
	private double totalamount;
    private List<BucketDTO> bucket;
	private String paymentmode;
	private int ispaymentdone;
	private String status;
	private String isdelevered;
}
