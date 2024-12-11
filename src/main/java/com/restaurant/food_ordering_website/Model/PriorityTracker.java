package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="priority_tracker")
@Data
public class PriorityTracker {
    @Id
    private int restaurant_id;

    @Column(name = "total_orders")
    private int order;

    @Column(name = "total_profit")
    private int profit;
}
