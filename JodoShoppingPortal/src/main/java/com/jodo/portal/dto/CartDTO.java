package com.jodo.portal.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    private long id;
    private double totalamount;
    private List<CartItemDTO> cartItems;
}
