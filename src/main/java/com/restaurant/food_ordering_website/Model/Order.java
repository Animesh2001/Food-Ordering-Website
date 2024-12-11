package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="customer_order")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer orderId;
    @Column(name = "group_order_id")
    private Integer groupOrderId;
    private String itemName;
    private int itemQuantity;
    private int itemPrice;
    private int itemTotal;
    private int restaurantId;
    private Integer userId;

}
