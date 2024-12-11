package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="customer_cart_details")
@Data
public class Cart {
    @Id
    private Integer orderId;
    private int totalValue;
    private Long date;
    private String status;
    private Integer restaurantId;
    private Integer userId;
}
