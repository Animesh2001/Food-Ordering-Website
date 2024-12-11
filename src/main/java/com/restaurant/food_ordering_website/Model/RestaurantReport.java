package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "restaurant_report")
@Data
public class RestaurantReport {
    @Id
    private int restaurantId;

    private String filePath;
}
