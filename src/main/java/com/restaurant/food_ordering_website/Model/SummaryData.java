package com.restaurant.food_ordering_website.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryData {

    private String restaurantName;
    private int totalOrders;
    private int totalValue;
}
