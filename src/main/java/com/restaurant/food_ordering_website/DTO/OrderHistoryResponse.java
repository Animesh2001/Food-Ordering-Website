package com.restaurant.food_ordering_website.DTO;

import com.restaurant.food_ordering_website.Model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {
    List<OrderHistory> orderItems;
    private Long DateInEpochTimeMillis;
    private String date;
    private String time;
    private String status;
    private int totalValue;
}
