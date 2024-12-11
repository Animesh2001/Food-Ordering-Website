package com.restaurant.food_ordering_website.NewModel;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Addresses", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "address_type"}))
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "address_type", length = 50, nullable = false)
    private String addressType;

    @Column(name = "address_line_1", length = 255, nullable = false)
    private String addressLine1;

    @Column(name = "address_line_2", length = 255, nullable = true)
    private String addressLine2;

    @Column(name = "country_name", length = 50, nullable = false)
    private String countryName;

    @Column(name = "pin_code", length = 10, nullable = false)
    private String pinCode;

    @Column(name = "city", length = 100, nullable = false)
    private String city;

    @Column(name = "state", length = 50, nullable = false)
    private String state;
}
