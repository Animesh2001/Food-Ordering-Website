package com.restaurant.food_ordering_website.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private int id;
    private String itemName;
    private int itemQuantity;
    private int itemPrice;
}
