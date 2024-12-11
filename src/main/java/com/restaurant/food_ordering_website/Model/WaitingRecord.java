package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "waiting_record")
@Data
public class WaitingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int restaurantId;

    private int orderId;
}
