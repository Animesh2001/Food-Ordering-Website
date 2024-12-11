package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "ordercount")
public class OrderCount {
    @Id
    @Column(name = "restaurantid")
    private int restaurantId;

    @Column(name = "totalordersprocessing")
    private int totalOrdersProcessing;
}
