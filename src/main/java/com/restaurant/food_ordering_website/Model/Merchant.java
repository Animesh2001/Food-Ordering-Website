package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="merchant")
@Data
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private int merchantId;
    private String email;
    private String password;
    private int credit = 0;
}
