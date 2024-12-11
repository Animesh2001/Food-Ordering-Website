package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "merchant_restaurant_mapping")
@Data
public class MerchantRestaurantMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "merchant_id")
    private int merchantId;
    @Column(name = "restaurant_id")
    private int restaurantId;
}
