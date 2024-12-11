package com.restaurant.food_ordering_website.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="customer")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="userid")
    private Integer userId;
    private String email;
    private String password;
    private String city;
    private int credit = 10000;
}
