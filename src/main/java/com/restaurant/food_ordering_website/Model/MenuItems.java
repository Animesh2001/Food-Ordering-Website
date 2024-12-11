package com.restaurant.food_ordering_website.Model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="menuitems")
public class MenuItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer restaurantId;
    private String itemName;
    private String description;

    @Column(name="quantity")
    private int quantityAvailable;

    @Column(name = "quantityunit")
    private String quantityUnit;

    private String price;

    private String additionalCustomizations;

    private String availableTime;

    private int maxQuantityPerOrder;

    @Column(name = "available")
    private boolean isAvailable = true;

}
