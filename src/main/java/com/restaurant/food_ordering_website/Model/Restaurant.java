package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="restaurant")
@Data
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int restaurantId;
    private String name;
    private String description;
    private String address;
    private String contact;
    @Column(name = "available")
    private boolean isAvailable = true;
    @Column(name = "merchantid")
    private Integer merchantId;
    @Column(name = "priority")
    private Integer priority = 0; // Default value in Java model
}
