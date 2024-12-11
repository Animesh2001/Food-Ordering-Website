package com.restaurant.food_ordering_website.DTO;

import lombok.Data;

@Data
public class OrderHistory {
    private String itemName;
    private int itemQuantity;
    private int itemPrice;
    private int itemTotalPrice;
}
